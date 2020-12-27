(ns ax.cljs.websocket
  (:require [taoensso.timbre :as timbre]
            [goog.object :as obj]
            [ax.cljs.transit :as tt]
            [clojure.pprint :as pp]))


(defonce *ws-connection (atom nil))

(defn on-open []
  (timbre/info "websocket open!"))

(defn on-error [x]
  (timbre/info "websocket error:" x))

(defn on-message [^js/Object ws-event]
  ;(timbre/info "websocket message:" ws-event)
  ;(timbre/info "type :::" (type ws-event))
  ;(timbre/spy (tt/<<transit (.-data ws-event)))
  )

(defn on-close [x]
  (timbre/info "websocket closed..." x))


(defn new-web-socket! []
  (let [^js/WebSocket ws (try
                           (js/WebSocket. "ws://192.168.1.62:80")
                           (catch js/Error e (do (timbre/info e) nil)))]
    (if (nil? ws)
      (timbre/warn "Could not get a websocket... That's annoying.")
      (doto ws
        (obj/set "onopen" on-open)
        (obj/set "onerror" on-error)
        (obj/set "onmessage" on-message)
        (obj/set "onclose" on-close)))
    ;save websocket instance in an atom
    (reset! *ws-connection ws)))

(defn send [ws-conn req]
  (.send ws-conn (tt/>>transit req)))

(defn close [ws-conn]
  (.close ws-conn))
