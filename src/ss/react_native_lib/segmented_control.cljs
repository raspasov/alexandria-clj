(ns ss.react-native-lib.segmented-control
 (:require ["@react-native-segmented-control/segmented-control$default" :as SegCtrl]
           [ss.react.core :as rc]))

(def seg-ctrl (partial rc/create-element-js SegCtrl))

