(ns ax.numbers
  (:require [clojure.edn :as edn]))


(defn string->number
  ([s] (string->number s nil))
  ([s default]
   (let [result (try
                  (edn/read-string s)
                  #?(:clj (catch Exception e e))
                  #?(:cljs (catch js/Error e e)))]
     (if (number? result)
       result
       default))))


(defn format-decimals [num-of-decimals x]
  #?(:clj
     (clojure.pprint/cl-format nil (str "~," num-of-decimals "f") x))
  #?(:cljs
     (cljs.pprint/cl-format nil (str "~," num-of-decimals "f") x)))
