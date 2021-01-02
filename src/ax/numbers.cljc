(ns ax.numbers
  (:require [clojure.edn :as edn]))


(defn string->number
  ([s] (string->number s nil))
  ([s default]
   (let [result (try
                  (edn/read-string s)
                  #?(:clj (catch Exception e e))
                  #?(:clj (catch Error e e)))]
     (if (number? result)
       result
       default))))


