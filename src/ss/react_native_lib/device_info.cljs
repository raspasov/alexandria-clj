(ns ss.react-native-lib.device-info
 (:require [react-native-device-info :as -info]
           [ss.numbers :as ss.n]
           [taoensso.timbre :as timbre]))

(def ^js/Object info -info)


(defn get-device-id []
 (.getDeviceId info))

(defn device-generation-ios
 "(Heuristic approach for iOS only)
  Tries to parse device generation from device id. If it fails, returns nil"
 []
 (try
  (let [s (get-device-id)
        [part1 _] (clojure.string/split s #",")]
   (ss.n/string->number
    (clojure.string/replace part1 #"\D" "")
    ;return
    nil))
  (catch js/Error e
   (do (timbre/warn "device-id->generation failed" e)
       ;return
       nil))))


(defn get-unique-id
 "IDFV. Unique device ID that stays the same for this app (in most cases)"
 []
 (.getUniqueId info))
