(ns ss.experimental.comment-line-count
 (:require [net.cgrand.xforms :as x]))


(defn count-comment-lines []
 (let [source (slurp "/Users/raspasov/cpp")
       lines  (line-seq
               (java.io.BufferedReader.
                (java.io.StringReader. source)))]
  (transduce
   (comp
    (partition-by
     (fn [line]
      (cond
       (clojure.string/includes? line "/*") :start
       (clojure.string/includes? line "*/") :end)))
    ;partition so we can access previous elements
    (x/partition 2 1 (x/into []))
    (map
     (fn [[[?comment-start-line] ?comment-lines :as v]]
      (if (clojure.string/includes? ?comment-start-line "/*")
       ;return number of commented lines
       (count ?comment-lines)
       ;else, return vector unchanged
       v)))
    ;only the numbers
    (filter number?))
   ;sum all the commented lines
   +
   lines)))

