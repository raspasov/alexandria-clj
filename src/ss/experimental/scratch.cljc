(ns ss.experimental.scratch
 (:require [ss.core :as ss.c]))


;Specter-like
(defn find-in-data []
 (let [x    {:au {:b [{:n 1 :k "q"} {:n 1 :k "w"} {:n 1 :k "e"}]}}
       path [:au :b]
       ;find idx based on pred
       idx (first
            (ss.c/positions
             ;pred
             (fn [m] (= "w" (:k m)))
             (get-in x path)))
       ;final path
       path' (conj path idx)]

  (update-in x path'
   (fn [m] (assoc m :hello :world)))))

