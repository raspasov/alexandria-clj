(ns ss.react-native-lib.ss-headphone-motion
 (:require
  [react-native$NativeModules :as -NativeModules]
  [react-native$NativeEventEmitter :as -NativeEventEmitter]
  [cljs-bean.core :as b]
  [clojure.core.async :as a]
  [ss.react-native.nat :as nat]
  [ss.react.core :as rc]
  [taoensso.timbre :as timbre]
  [ss.numbers :as ss.n]))


(def ^js SSHeadphoneMotion (.-SSHeadphoneMotionManager nat/NativeModules))


(defn start-updates []
 (a/go
  (timbre/spy
   (a/<! (.startUpdates SSHeadphoneMotion "")))))

(defn stop-updates []
 (a/go
  (timbre/spy
   (a/<! (.stopUpdates SSHeadphoneMotion "")))))


;Listeners
;---------------------------------------------------------
(def ^js NativeEventEmitter -NativeEventEmitter)

(def ^js Emitter (NativeEventEmitter. SSHeadphoneMotion))

(defn headphone-motion-listener [data]
 (let [{{:keys [x y z timestamp]} :userAcceleration} (b/->clj data)
       timestamp' (ss.n/format-decimals 3 timestamp)
       y'         (ss.n/format-decimals 2 y)]
  (timbre/spy [timestamp' y'])))

(defn add-listener [event-name f]
 (.addListener Emitter event-name f))

(defn remove-listener
 "Removes a SPECIFIC listener for an event-name.
  IMPORTANT: Original callback fn must be provided."
 [event-name f]
 (.removeListener Emitter event-name f))

(defn remove-all-listeners [event-name]
 (.removeAllListeners Emitter event-name))
