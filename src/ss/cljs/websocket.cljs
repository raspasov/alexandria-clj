(ns ss.cljs.websocket
 (:require [taoensso.timbre :as timbre]
           [goog.object :as obj]
           [ss.cljs.transit :as tt]
           [ss.numbers :as axn]
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


(defn new-web-socket! [{:keys [ws-url]}]
 (let [^js/WebSocket
       ws (try
           (js/WebSocket. ws-url)
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
