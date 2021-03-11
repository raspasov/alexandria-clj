(ns ax.expo.camera
  (:require [expo-camera :as -expo-camera]))


(def ^js/Object expo-camera -expo-camera)
(def Camera (.-Camera expo-camera))


