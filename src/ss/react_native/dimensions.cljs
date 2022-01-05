(ns ss.react-native.dimensions
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

(defn portrait? [{:keys [width height]}]
 (< width height))

(defn landscape? [{:keys [width height] :as w}]
 (not (portrait? w)))


(defn ww [] (:width (get-window)))
(defn ww-half [] (/ (ww) 2))
(defn hh [] (:height (get-window)))
;(defn hh-half [] (/ (hh) 2))

(defn ww-2 [window] (:width window))
(defn ww-half-2 [window] (/ (:width window) 2))
(defn hh-2 [window] (:height window))
(defn hh-half-2 [window]
 (/ (:height window) 2))

(defn get-device-relative-scale []
 (/ (.-width (get-window-obj)) 375))

(defn get-device-pixel-density []
 (.get PixelRatio))

(defn get-pixel-size-for-layout-size [x]
 (.getPixelSizeForLayoutSize PixelRatio x))

(defn- -<>
 ([window scalar-value]
  (* scalar-value (/ (:width window) 375)))
 ([scalar-value]
  (* scalar-value (get-device-relative-scale))))

(def <> (memoize -<>))


(defn ipad? [window]
 (or
  (< 1000 (:width window))
  (< 1000 (:height window))))


(defn <>2
 ([scalar-value]
  (<>2 (get-window) scalar-value))
 ([window scalar-value]
  (let [x    (if (ipad? window) 2 1)
        base (if (ipad? window) 500 (:width window))]
   (* scalar-value (/ (:width window) (* x base))))))

(defn- ->< [scalar-value] (/ scalar-value (get-device-relative-scale)))
(def >< (memoize -><))


(defn pre-iphone-x? []
 (< (hh) 812))


(defn iphone-x+? []
 (not (pre-iphone-x?)))


(defn abs<>
 "Adjusts absolute positioned values to avoid positioning within the safe ares"
 [window scalar-value style-attr]
 (if (ipad? window)
  ;return unchanged
  scalar-value
  ;else
  (let [l? (landscape? window)
        p? (portrait? window)]
   (cond
    (and l? (= style-attr :t)) scalar-value
    (and l? (= style-attr :b)) scalar-value
    (and l? (= style-attr :l)) (+ scalar-value 44)
    (and l? (= style-attr :r)) (+ scalar-value 44)

    (and p? (= style-attr :t)) (+ scalar-value 44)
    (and p? (= style-attr :b)) (+ scalar-value 44)
    (and p? (= style-attr :l)) scalar-value
    (and p? (= style-attr :r)) scalar-value

    :else scalar-value))))
