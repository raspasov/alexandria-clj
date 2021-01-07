(ns ax.react-native-lib.gesture-handler
  (:require [react-native-gesture-handler :as -rngh]
            [ax.react.core :as rc]
            [goog.object :as obj]))

(def ^js/Object rngh -rngh)

(def ^js/Object State (.-State rngh))
(def BEGAN (.-BEGAN State))
(def FAILED (.-FAILED State))
(def ACTIVE (.-ACTIVE State))
(def UNDETERMINED (.-UNDETERMINED State))
(def CANCELLED (.-CANCELLED State))
(def END (.-END State))

(def number->state-m
  {0 "UNDETERMINED", 1 "FAILED", 2 "BEGAN", 3 "CANCELLED", 4 "ACTIVE", 5 "END"})


(def pan (partial rc/create-element-js (obj/get rngh (name :PanGestureHandler))))
