(ns ax.react-native-lib.animatable
  (:require [react-native-animatable :as -rna]
            [ax.react.core :as rc]))

(def ^js/Object rna -rna)


(def rna-view (partial rc/create-element-js (.-View rna)))


(def rna-text (partial rc/create-element-js (.-Text rna)))
