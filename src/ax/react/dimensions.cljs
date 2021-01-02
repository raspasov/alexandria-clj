(ns ax.react.dimensions
  (:require [react-native :as rn]))

(def ^js/Object ReactNative rn)

(def ^js/Object Dimensions (.-Dimensions ReactNative))

(def ^js/Object PixelRatio (.-PixelRatio ReactNative))

(defn ^js/Object get-window-obj []
   (.get Dimensions "window"))

(defn get-window []
  (let [^js/Object window (get-window-obj)]
    {:width  (.-width window)
     :height (.-height window)
     :scale  (.-scale window)}))

(defn width [] (:width (get-window)))
(defn height [] (:height (get-window)))

(defn get-device-relative-scale []
  (/ (.-width (get-window-obj)) 375))

(defn get-device-pixel-density []
  (.get PixelRatio))

(defn get-pixel-size-for-layout-size [x]
  (.getPixelSizeForLayoutSize PixelRatio x))

(defn- -<> [scalar-value]
  (* scalar-value (get-device-relative-scale)))

(def <> (memoize -<>))

(defn- ->< [scalar-value] (/ scalar-value (get-device-relative-scale)))
(def >< (memoize -><))
