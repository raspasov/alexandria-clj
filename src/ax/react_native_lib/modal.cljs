(ns ax.react-native-lib.modal
  (:require [react-native-modal :as -rnm]
            [ax.react.core :as rc]))


(def ^js/Object rnm -rnm)
(def modal-view (partial rc/create-element-js (.-default rnm)))

