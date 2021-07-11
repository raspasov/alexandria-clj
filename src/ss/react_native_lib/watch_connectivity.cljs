(ns ss.react-native-lib.watch-connectivity
  (:require [react-native-watch-connectivity :as -rnwc]
            [clojure.core.async :as a]
            [cljs-bean.core :as b :refer [bean ->clj ->js]]
            [taoensso.timbre :as timbre]))


(defonce *watch-reachability-listener-unsubscribe-fn (atom nil))
(defonce *watch-event-listener-unsubscribe-fn (atom nil))

(def ^js/Object rnwc -rnwc)


(def getReachability (.-getReachability rnwc))

(def getIsWatchAppInstalled (.-getIsWatchAppInstalled rnwc))

(def getIsPaired (.-getIsPaired rnwc))

(def getApplicationContext (.-getApplicationContext rnwc))

(def ^js/Object watchEvents (.-watchEvents rnwc))

(def addListener (.-addListener watchEvents))

(def onWatchEvent (.-on watchEvents))

(def sendMessage (.-sendMessage rnwc))


(defn init-watch-reachability-listener [update-reachability-fn]
  (timbre/info "Init Apple Watch Reachability listener...")
  (let [?unsubscribe-fn @*watch-reachability-listener-unsubscribe-fn]
    (when (fn? ?unsubscribe-fn)
      (?unsubscribe-fn)))

  (reset! *watch-reachability-listener-unsubscribe-fn
    (addListener
      "reachability"
      (fn [reachable?]
        (when (fn? update-reachability-fn)
          (update-reachability-fn reachable?))
        (timbre/info "updating reachability...")
        (timbre/spy reachable?))))

  (a/go
    (let [[reachable? ?error :as reachability] (a/<! (getReachability))]
      ;update reachability once
      (update-reachability-fn reachable?))))


(defn init-watch-event-listener [f]
  (timbre/info "Init Apple Watch Event listener...")
  (let [?unsubscribe-fn @*watch-event-listener-unsubscribe-fn]
    (when (fn? ?unsubscribe-fn)
      (?unsubscribe-fn)))

  (reset! *watch-event-listener-unsubscribe-fn (onWatchEvent "message" f)))


(defn send-message [m response-f]
  (sendMessage
    (b/->js m)
    (fn [watch-response]
      (let [watch-response' (b/->clj watch-response)]
        (timbre/spy watch-response')
        (response-f watch-response')))
    (fn [watch-error]
      (timbre/spy (b/->clj watch-error)))))


;REPL usage
;-------------------------
(defn message-test []
  (a/go
    (let [_ (sendMessage
              (b/->js {:reset-max-magnitude "resetting..."})
              (fn [watch-response]
                (let [watch-response' (b/->clj watch-response)]
                  (timbre/spy watch-response')))
              (fn [watch-error]
                (timbre/spy (b/->clj watch-error))))])))


(defn watch-test []
  (a/go
    (let [[reachable? ?error :as reachability] (a/<! (getReachability))]
      (timbre/spy reachability)))

  (a/go
    (let [is-watch-app-installed (a/<! (getIsWatchAppInstalled))]
      (timbre/spy is-watch-app-installed)))

  (a/go
    (let [is-paired (a/<! (getIsPaired))]
      (timbre/spy is-paired)))

  (a/go
    (let [app-context (a/<! (getApplicationContext))]
      (timbre/spy app-context)))

  )



