(ns ss.expo.sensors
 (:require [expo-sensors :as -sensors]
           [clojure.core.async :as a]
           [taoensso.timbre :as timbre]
           [ss.react.core :as rc]
           [cljs-bean.core :as b]))

(def ^js sensors -sensors)
(def ^js DeviceMotion (.-DeviceMotion sensors))



(defn is-available? []
 (a/go
  (timbre/spy
   (a/<! (.isAvailableAsync DeviceMotion)))))


(defn listener [set-device-motion]
 (fn [^js o]
  ;(timbre/info "setting device-motion...")
  (set-device-motion (b/->clj o))))


(defn use-device-motion
 ([update-interval-ms]
  (use-device-motion update-interval-ms identity))
 ([update-interval-ms set-device-motion-f]
  (let [[device-motion set-device-motion] (rc/use-state nil)
        set-device-motion' (comp set-device-motion set-device-motion-f)
        _                  (.setUpdateInterval DeviceMotion update-interval-ms)
        _                  (rc/use-effect-once
                            (fn []
                             (timbre/info "use-device-motion init")
                             (let [^js subscription (.addListener DeviceMotion (listener set-device-motion'))]
                              (fn cleanup []
                               (timbre/info "use-device-motion cleanup")
                               (.remove subscription)))))]
   device-motion)))
