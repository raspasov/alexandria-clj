(ns ax.react-native-lib.animatable
  (:require [react-native-animatable :as -rna]
            [ax.react.core :as rc]
            [cljs-bean.core :as b]
            [ax.react-native.dimensions :as dm]))

(def ^js/Object rna -rna)


(def rna-view (partial rc/create-element-js (.-View rna)))


(def rna-text (partial rc/create-element-js (.-Text rna)))


(def animatable-registry
  (.initializeRegistryWithDefinitions
    rna
    (b/->js
      {:removeFromList {:useNativeDriver true
                        :from            {:height (dm/<> 100)} :to {:height 0} :duration 200}})))
