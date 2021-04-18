(ns ss.react-native.animated
  (:require [ss.react-native.core :as rn]
            [ss.react.state-fns :as ax|state-fns]))


(def animated (.-Animated rn/ReactNative))

(def easing (.-Easing rn/ReactNative))

(def event (.-Animated.event rn/ReactNative))

(defn timing [an-animated-value m]
  (let [f (.-Animated.timing rn/ReactNative)]
    (when an-animated-value
      (f an-animated-value (clj->js (assoc m :useNativeDriver true))))))

(defn start
  ([^js/Object x]
   (when x (.start x)))
  ([^js/Object x ^IFn cb]
   (when (and x cb) (.start x cb))))

(defn new-value
  ([^js/Number x]
   (new-value x nil))
  ([^js/Number x k-or-ks]
   (let [c (.-Animated.Value rn/ReactNative)
         anim-value (c. x)]
     (when k-or-ks
       (ax|state-fns/set-mutable! k-or-ks anim-value))
     ;move to native
     (start
       (timing anim-value {:toValue x :duration 1}))
     anim-value)))


(defn value [k-or-ks]
  (ax|state-fns/get-mutable k-or-ks))

(defn math* [^js/Object anim-value-x ^js/Object anim-value-y]
  (let [f (.-Animated.multiply rn/ReactNative)]
    (f anim-value-x anim-value-y)))

(defn math+ [^js/Object anim-value-x ^js/Object anim-value-y]
  (let [f (.-Animated.add rn/ReactNative)]
    (f anim-value-x anim-value-y)))


(defn set-value [^js/Object anim-value ^js/Number x]
  (if (instance? js/Object anim-value)
    (.setValue anim-value x)
    #_(timbre/warn (ex-info "anim-value is not an Animated value" {:arguments [anim-value x]}))))

(defn ie
  ([^js/Object anim-value input-range output-range extrapolate]
   (ie anim-value input-range output-range extrapolate nil))
  ([^js/Object anim-value input-range output-range extrapolate default-value]
   (if (instance? js/Object anim-value)
     (.interpolate
       anim-value
       (clj->js {:inputRange input-range :outputRange output-range :extrapolate extrapolate}))
     (do
       #_(timbre/warn (ex-info "anim-value is not an Animated value" {:arguments [anim-value input-range output-range]}))
       default-value))))

