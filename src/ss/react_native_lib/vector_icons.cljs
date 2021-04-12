(ns ss.react-native-lib.vector-icons
  (:require [react-native-vector-icons$Ionicons :as ionicons]
            [ss.react.core :as rc]))


(def ionicons-view (partial rc/create-element-js ionicons))
