(ns ss.react.core
 (:require [ss.core :as ss.c]))


(defmacro e
 "Creates a React element with displayName"
 [component]
 (let [fully-qualified-name# (str *ns* "/" component)]
  `(partial
    ss.react.core/create-element-cljs
    (ss.react.core/memo
     (ss.cljs.googc/assoc-obj! ~component "displayName" ~fully-qualified-name#)))))


(defmacro with-keys
 "Takes React elements and adds :key to each one at compile time."
 [& elements]
 (vec
  (map-indexed
   (fn [idx# element#]
    (if (and
         (list? element#)
         (map? (second element#)))
     (apply list
      (map-indexed
       (fn [idx-2# m#]
        (if (= 1 idx-2#)
         (assoc m# :key idx#)
         m#))
       element#))
     element#))
   elements)))


(defmacro defnrc
 "A simple macro which outputs a (defn ... ) like this:

 ;from
 (defnrc my-component [props]
  ...)

  ;to
 (defn my-component [props]
  (let [props (rc/props props)]
   ...))

 "
 [& args]
 (let [ret#             `(defn ~@args)
       ;find the args index (in case of docstrings, etc)
       args-vector-idx# (first (ss.c/positions vector? ret#))
       ;the args themselves
       args-vector#     (nth ret# args-vector-idx#)
       ;split (defn ...) into parts
       ;each part-N is a sequence so we can concat them back up at the end
       part-1-defn#     (take args-vector-idx# ret#)
       part-2-args#     (vector args-vector#)
       part-3-body#     (drop (inc args-vector-idx#) ret#)
       ;MAIN PART: modify part 3 by "shadowing" the first argument
       part-3-body'#    (vector
                         `(let [~(first args-vector#)
                                (ss.react.core/props ~(first args-vector#))]
                           ~@part-3-body#))]
  ;concat all parts and return
  (concat
   part-1-defn#
   part-2-args#
   part-3-body'#)))






