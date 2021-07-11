(ns ss.react-native-lib.modalize
 (:require [react-native-modalize :as -modalize]
           [ss.react.core :as rc]))

(def ^js/Object modalize -modalize)

(def view (partial rc/create-element-js (.-Modalize modalize)))


