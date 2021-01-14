(ns ax.react-native-lib.progress
  (:require [react-native-progress :as -rnp]
            [ax.react.core :as rc]))


(def ^js/Object rnp -rnp)

(def progress-circle (.-Circle rnp))

(def progress-circle-view (partial rc/create-element-js progress-circle))

(comment
  (progress-circle-view
    {:size        50
     :progress    0.3
     :borderWidth 0
     :color       "red"
     }))
