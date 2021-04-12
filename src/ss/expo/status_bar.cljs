(ns ss.expo.status-bar
  (:require [expo-status-bar :as -expo-status-bar]))


(def ^js/Object expo-status-bar -expo-status-bar)


(defn set-status-bar-hidden [t-or-f]
  (.setStatusBarHidden expo-status-bar t-or-f))
