(ns ss.react-native-lib.threads
  (:require
    [react-native-threads :as -rnt]
    [taoensso.timbre :as timbre]))


(def rnt ^js/Object -rnt)
(def Thread (.-Thread rnt))

(defonce *thread (atom nil))

(defn new-thread []
  (let [thread (new Thread "./index.thread.js")]
    (reset! *thread thread)
    (timbre/spy thread)))



