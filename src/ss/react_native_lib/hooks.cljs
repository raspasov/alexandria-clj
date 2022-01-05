(ns ss.react-native-lib.hooks
 (:require ["@react-native-community/hooks" :as -hooks]))

(def ^js/Object hooks -hooks)


(defn ^js/Object useKeyboard []
 ((.-useKeyboard hooks)))


(defn useDimensions []
 ((.-useDimensions hooks)))
