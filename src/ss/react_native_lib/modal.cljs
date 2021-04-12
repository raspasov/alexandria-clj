(ns ss.react-native-lib.modal
  (:require [react-native-modal :as -rnm]
            [ss.react.core :as rc]))


(def ^js/Object rnm -rnm)
(def modal-view (partial rc/create-element-js (.-default rnm)))

