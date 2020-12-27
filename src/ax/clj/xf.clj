(ns ax.clj.xf
  (:import (java.util ArrayDeque)))


(defn sliding
  ([^long n] (sliding n 1))
  ([^long n ^long step]
   (fn [rf]
     (let [a (ArrayDeque. n)]
       (fn
         ([] (rf))
         ([result]
          (let [result (if (.isEmpty a)
                         result
                         (let [v (vec (.toArray a))]
                           ;;clear first!
                           (.clear a)
                           (unreduced (rf result v))))]
            (rf result)))
         ([result input]
          (.add a input)
          (if (= n (.size a))
            (let [v (vec (.toArray a))]
              (dorun (take step (repeatedly #(.removeFirst a))))
              (rf result v))
            result)))))))

