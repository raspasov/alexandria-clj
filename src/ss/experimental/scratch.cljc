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
               (fn [m] (= "w" (:k m)))
               (get-in x path)))
       ;final path
       path' (conj path idx)]

  (update-in x path'
   (fn [m] (assoc m :hello :world)))))



(defn my-func []
 (async/<!! (async/timeout 1000))
 (println "Current Thread :::" (.getName (Thread/currentThread))))

(comment
 ;bad
 (async/go (my-func)))
;=>
;Current Thread ::: async-dispatch-1

(comment
 ;good
 (async/go
  (async/<! (async/thread (my-func)))))
;=>
;Current Thread ::: async-thread-macro-1


(let [source (slurp "/Users/raspasov/cpp")
        lines  (line-seq
                (java.io.BufferedReader.
                 (java.io.StringReader. source)))]
   (sequence
    (comp
     ;find start of comment
     (drop-while
      (fn [line]
       (not (clojure.string/includes? line "/*"))))
     ;find end of comment
     (take-while
      (fn [line]
       (not (clojure.string/includes? line "*/")))))
    lines))

(let [source (slurp "/Users/raspasov/cpp")
      lines  (line-seq
              (java.io.BufferedReader.
               (java.io.StringReader. source)))]
 (transduce
  (comp
   (partition-by
    (fn [line]
     (timbre/spy line)
     (cond
      (clojure.string/includes? line "/*") :start
      (clojure.string/includes? line "*/") :end)))
   (net.cgrand.xforms/partition 2 1 (x/into []))
   (map
    (fn [[[?comment-start] ?comment-lines :as v]]
     (timbre/spy ?comment-start)
     (if (clojure.string/includes? ?comment-start "/*")
      ;return number of commented lines
      (count ?comment-lines)
      v)))
   (filter number?))
  +
  lines))
