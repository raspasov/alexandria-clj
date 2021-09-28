(ns ss.react-native-lib.reanimated-v2
 (:require [box :as -box]
           [ss.react.core :as rc]
           [react-native-reanimated :as -reanimated]))


(def ^js/Object reanimated -reanimated)
(def withTiming (.-withTiming reanimated))
(def withSpring (.-withSpring reanimated))
(def withDecay (.-withDecay reanimated))
(def cancelAnimation (.-cancelAnimation reanimated))

(def ^js/Object box -box)
(def view (partial rc/create-element-js (.-Box box)))
