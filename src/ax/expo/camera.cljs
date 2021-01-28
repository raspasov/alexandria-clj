(ns ax.expo.camera
  (:require [expo-camera :as -expo-camera]
            [goog.object :as obj]))


(def ^js/Object expo-camera -expo-camera)
(def Camera (obj/get expo-camera (name :Camera)))


(comment
  (a/go
    (let [[result error?] (a/<! (.requestPermissionsAsync Camera))]
      (reset! *tmp result)
      (timbre/spy (js->clj result :keywordize-keys true))
      (timbre/spy error?))))
