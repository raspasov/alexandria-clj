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


(defn running-total
  "Transforms a sequence of numbers to a vector with running total sum."
  [seq-of-numbers]
  (transduce
    (map identity)
    (completing
      (fn [accum x]
        (let [x-prev (or (peek accum) 0)]
          (conj accum (+ x x-prev)))))
    []
    seq-of-numbers))
