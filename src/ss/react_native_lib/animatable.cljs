(ns ss.react-native-lib.animatable
  (:require [react-native-animatable :as -rna]
            [ss.react.core :as rc]
            [cljs-bean.core :as b]
            [ss.react-native.dimensions :as dm]))

(def ^js/Object rna -rna)


(def view (partial rc/create-element-js (.-View rna)))


(def text (partial rc/create-element-js (.-Text rna)))


(def animatable-registry
  (.initializeRegistryWithDefinitions
    rna
    (b/->js
      {:removeFromList {:useNativeDriver true
                        :from            {:height (dm/<> 100)} :to {:height 0} :duration 200}})))
