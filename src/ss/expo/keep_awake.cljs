(ns ss.expo.keep-awake
  (:require [expo-keep-awake :as -keep-awake]))


(def ^js/Object keep-awake -keep-awake)


(defn activate-keep-awake []
  (.activateKeepAwake keep-awake))


(defn deactivate-keep-awake []
  (.deactivateKeepAwake keep-awake))
