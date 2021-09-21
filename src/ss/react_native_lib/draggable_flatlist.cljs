(ns ss.react-native-lib.draggable-flatlist
 (:require [react-native-draggable-flatlist$default :as DraggableFlatList]
           [taoensso.timbre :as timbre]
           [ss.react.core :as rc]))

(def draggable-flatlist (partial rc/create-element-js DraggableFlatList))
