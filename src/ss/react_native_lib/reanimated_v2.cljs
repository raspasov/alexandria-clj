(ns ss.react-native-lib.reanimated-v2
  (:require [box :as -box]
            [ss.react.core :as rc]))


(def ^js/Object box -box)
(def view (partial rc/create-element-js (.-Box box)))
