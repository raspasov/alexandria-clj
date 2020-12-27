(ns ax.clj.http-next
  "HTTP request/response framework in less than 200 lines."
  (:require [byte-streams :as bs]
            [ring.util.response :as rr]
            [clojure.core.async :as a :refer [<! go chan put! close!]]
            [clojure.core.async.impl.protocols :refer [closed?]]
            [cheshire.core :as cheshire]
            [manifold.deferred :as d]
            [taoensso.timbre :refer [info spy]]
            [ring.util.response :as ring-response]
            [ax.clj.transit :as tt-clj]
            [manifold.stream :as manifold-stream]
            [taoensso.timbre :as timbre]
            [aleph.http :as aleph-http]
            [ax.clj.websocket-state :as ws-state])
  (:import (clojure.lang IFn Keyword)
           (clojure.core.async.impl.channels ManyToManyChannel)))


;shortcut
(def json cheshire/generate-string)

(defprotocol ApiCore
  (->dispatch-f [this req] "Makes a fn into a dispatch fn")
  (->error-f [this error] "Makes a fn into an error fn"))

(extend-protocol ApiCore
  IFn
  (->dispatch-f [this req]
    (fn [] (this req)))
  (->error-f [this ^Throwable e]
    (fn [] (this e))))

(defn ^String get-error-msg [^Throwable e]
  (let [{:keys [trace]} (Throwable->map e)
        ^String -msg (let [msg (.getMessage e)] (if (string? msg)
                                                  msg
                                                  (do
                                                    "That was a bad error, we have no details..."
                                                    (info e))))
        error-msg    (clojure.string/replace -msg #"java.lang." "")]
    error-msg))

(defn default-error-f
  "Default error handler.
   Gets called anytime an HTTP req/resp sequence of function calls generates an exception."
  [^Throwable e]
  (let [{:keys [trace]} (Throwable->map e)
        error-msg (get-error-msg e)]
    (cond
      ;slow down
      (clojure.string/includes? error-msg "could not serialize")
      (rr/status
        (rr/response
          {:error (str "Speed of light seems too slow for you! " error-msg)})
        429)
      ;any error
      :else
      (do
        (timbre/warn e)
        (rr/status
          (rr/response
            {:error (str error-msg) :trace (str trace)})
          520)))))

;Transit response/request parser/encoder
;=======================================================================================================================
(defn transit-response-f [resp]
  ;(info transit-response-f)
  (-> resp
      (update-in [:body] #(tt-clj/data-to-transit %))
      (update-in [:headers] (fn [headers] (assoc headers "content-type" "application/transit+json")))))

(defn transit-request-f [req]
  (-> req
      (update-in [:body] bs/to-string)
      (update-in [:body] #(tt-clj/transit-to-data %))))

(defn bs->string [bs]
  (let [s (bs/to-string bs)]
    s))

;Default response/request parser/encoder
;=======================================================================================================================
(defn- body->data
  "Takes an Aleph body (an InputStream) of bytes and transforms it to an immutable data structure"
  [^bytes body]
  (some-> body bs->string (cheshire/parse-string true)))

(defn default-request-f
  "Request"
  [req]
  (update-in req [:body] (fn [body] (body->data body))))

(defn default-response-f
  "Response"
  [resp]
  (update-in resp [:body] #(json %)))
;=======================================================================================================================
(defn ws-request-f [req]
  (tt-clj/transit-to-data req))

(defn ws-response-f
  "Response"
  [resp]
  (tt-clj/data-to-transit resp))

(defn ws-error-f
  [^Throwable e]
  (let [{:keys [trace]} (Throwable->map e)
        error-msg (get-error-msg e)]
    (rr/status
      (rr/response {:error (str error-msg) :trace (str trace)})
      520)))


(defn unsupported-resp [x]
  (println "unsupported response::" x)
  (Exception. (str "Response of type " (type x) " is not supported, return a Ring-compatible response")))


(defn prepare-response [responce-ch-or-data]
  (go
    (let [response  (if (instance? ManyToManyChannel responce-ch-or-data)
                      ;channel response, take value from it
                      (<! responce-ch-or-data)
                      ;response is directly data
                      responce-ch-or-data)
          ;response must be either an exception or a Ring-compatible response
          response' (if-not (or (instance? Throwable response)
                                (ring-response/response? response))
                      ;unsupported response
                      (unsupported-resp response)
                      ;response is good
                      response)
          ;optionally opt out of modifying the response
          {:keys [unmodified?]} (meta response')]
      response')))

(defn service-api
  "Builds a service API.
   Params:
   req - the request as a map, like {:status 200, :body ... etc}
   dispatch-f - router fn, specific per service
   request-f - takes an incoming request, optionally modifying it, for example JSON parsing of body
   response-f - takes a response body, modifying it, for example Clojure data -> JSON string transformation
   error-f - handles any exception that might happen during the execution of 'dispatch-f'"
  [req dispatch-f request-f response-f error-f ^Keyword http-or-ws ws-chans]
  (let [deferred-or-chan (if (= :http http-or-ws)
                           ;http
                           (d/deferred)
                           ;websocket
                           (get ws-chans :output-ch))
        success!-or-put! (if (= :http http-or-ws)
                           ;http
                           d/success!
                           ;websocket
                           put!)
        ;safely parse the request
        req'             (try (request-f req) (catch Exception e e))
        ;_ (println "api-core/init, body':" body')
        responce-ch-or-data
                         (if (instance? Throwable req')
                           ;exception while parsing request
                           req'
                           ;all good, proceed to actual API call
                           (try
                             ;try safely
                             ((->dispatch-f dispatch-f req'))
                             (catch Exception e e)))]
    ;wait for return-ch and fulfill the deferred
    (do
      (go
        (let [response (if (instance? ManyToManyChannel responce-ch-or-data)
                         ;channel response, take value from it
                         (<! responce-ch-or-data)
                         ;response is directly data
                         responce-ch-or-data)
              ;response must be either an exception or a Ring-compatible response
              response (if-not (or (instance? Throwable response)
                                   (ring-response/response? response))
                         ;unsupported response
                         (unsupported-resp response)
                         ;response is good
                         response)
              ;optionally opt out of modifying the response
              {:keys [unmodified?]} (meta response)]
          ;fulfill the deferred
          (if (instance? Throwable response)
            (do
              (success!-or-put! deferred-or-chan (response-f ((->error-f error-f response)))))
            ;all good, check if we should modify the response or not
            ;individual endpoints can opt-out of modifying by returning (with-meta resp {:unmodified? true}),
            ;for example, if the endpoint is a redirect or an html page
            (let [resp (if (= true unmodified?) response (response-f response))]
              (success!-or-put! deferred-or-chan resp)))))
      ;return manifold deferred or chan
      deferred-or-chan)))


(defn new-web-socket-loop! [req dispatch-f ws-chans]
  (let [{{:keys [account]} :identity body :body headers :headers} req
        {:keys [sec-websocket-protocol sec-websocket-key]} headers
        uuid (or account (str (java.util.UUID/randomUUID)))]
    (timbre/info "New websocket loop starting...")
    (timbre/spy sec-websocket-key)
    ;Add socket state
    (ws-state/add-socket uuid sec-websocket-key ws-chans)
    (a/go
      (loop []
        (let [req (a/<! (:input-ch ws-chans))]
          (if (nil? req)
            ;got nil, web socket is closed
            (do
              (timbre/info "websocket closed...")
              ;Remove socket state
              (ws-state/remove-socket uuid sec-websocket-key))
            ;else, "req" received on web socket
            (do
              (a/thread (dispatch-f req ws-chans))
              (recur))))))))


(defn http-or-ws
  "Handler for all WebSocket or HTTP incoming connections"
  [{:keys [headers] :as req} http-dispatch-f ws-dispatch-f]
  (if (= "websocket" (get headers "upgrade"))
    ;websocket request
    (do
      (timbre/info "WebSocket !")
      (timbre/spy req)
      (let [{:keys [sec-websocket-protocol]} headers
            manifold-deferred @(aleph-http/websocket-connection req)
            input-ch          (a/chan 1024)
            output-ch         (a/chan 1024)]
        ;connect streams
        (manifold-stream/connect manifold-deferred input-ch)
        (manifold-stream/connect output-ch manifold-deferred)
        ;forward for processing
        (new-web-socket-loop! req ws-dispatch-f {:input-ch input-ch :output-ch output-ch})))

    ;else, the request is HTTP, dispatch directly
    (do
      (timbre/info "HTTP !")
      (let [resp (http-dispatch-f req)]
        ;HTTP - return manifold deferred value
        (d/let-flow [resp-deferred resp]
                    resp-deferred)))))
