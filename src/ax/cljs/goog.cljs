(ns ax.cljs.goog
  (:require [goog.object :as obj]))


(defn get-in-obj [obj ks]
  (apply obj/getValueByKeys obj (mapv name ks)))
