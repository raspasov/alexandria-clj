(ns ss.cljs.gg
  (:require [goog.object :as gobj]))


(defn get-in-obj [o ks]
  (apply gobj/getValueByKeys o (mapv name ks)))

(defn assoc-obj! [o k v]
  (gobj/set o (name k) v)
  ;return object
  o)
