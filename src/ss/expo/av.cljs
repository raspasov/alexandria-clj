(ns ss.expo.av
 (:require [expo-av :as -expo-av]
           [ss.react.core :as rc]))

(def ^js/Object expo-av -expo-av)

(def video (partial rc/create-element-js (.-Video expo-av)))
