(ns ss.experimental.scratch
 (:require [ss.core :as ss.c]
           [medley.core :as med]
           [clojure.core.async :as async]
           [net.cgrand.xforms :as x]
           [taoensso.timbre :as timbre]))




;Specter-like
(defn find-in-data []
 (let [x     {:au {:b [{:n 1 :k "q"} {:n 1 :k "w"} {:n 1 :k "e"}]}}
       path  [:au :b]
       ;find idx based on pred
       idx   (first
              (ss.c/positions
               ;pred
               #(= "w" (:k %))
               (get-in x path)))
       ;final path
       path' (conj path idx)]


  (update-in x path'
   (fn [m] (assoc m :hello :world)))))


(defn pattern-1 [v1 v2]
 (reduce
  (fn [accum [x1 x2 :as item]]
   (if-let [?entry (find accum x1)]
    (if (= item ?entry)
     accum
     (reduced :no-match))
    (assoc accum x1 x2)))
  {}
  (sequence
   (map vector)
   v1 v2)))


#_(defn
   backwards
   "doesn't do much"
   [args]
   (apply str
    (vec (drop 1 (vec (reverse args))))))

#_(defn -main-1
   "I don't do a whole lot ... yet."
   [& args]
   (do
    (println (backwards '(1 2 3 str)))))

(defn
 backwards
 "doesn't do much"
 [args]
 (let [args         (reverse args)
       f            (nth args 0)
       numbers-data (drop 1 args)]
  (apply f numbers-data)))

(defn -main-2
 "I don't do a whole lot ... yet."
 [& args]
 (println (backwards [1 2 3 str])))


