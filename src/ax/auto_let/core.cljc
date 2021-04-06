(ns ax.auto-let.core
 (:require [taoensso.timbre :as timbre]))


(def conjv (fnil conj []))

(defn has-empty-space? [s]
 (boolean (some #(= " " %) (seq s))))


(defn can-destructure-string?
 "Naively try to determine if string can be used for destructuring.
  Incomplete."
 [s]
 (boolean
  (and
   ;check if not empty
   (seq s)
   ;ensure no empty space
   (not (some #(= " " %) (seq s))))))


(defn pprint-let
 "Basic pretty print for let statements"
 [v-for-let]
 (transduce
  (comp
   (partition-all 2)
   (map vec)
   (interpose "\n")
   (mapcat identity))
  (completing
   (fn [accum item]
    (conj accum item))
   (fn [accum-final]
    (println accum-final)))
  []
  v-for-let))


(defn local+local-indexed
 [a-key locals]
 (let [-new-local         (clojure.string/join "-"
                           (filter string?
                            [(namespace a-key) (name a-key)]))
       new-local-no-index (symbol -new-local)
       new-local-indexed  (if (contains? @locals new-local-no-index)
                           (do
                            (symbol (str -new-local "-" (get @locals new-local-no-index))))
                           new-local-no-index)]
  [new-local-indexed new-local-no-index]))


(defn- key->?local [a-key locals]
 (let [?local (cond
               (and
                (keyword? a-key)
                (qualified-keyword? a-key))
               (keyword (namespace a-key) "keys")

               (and
                (keyword? a-key)
                (not (qualified-keyword? a-key)))
               (keyword "keys")

               (and
                (string? a-key)
                (can-destructure-string? a-key))
               (keyword "strs")

               (and
                (symbol? a-key)
                (qualified-symbol? a-key))
               (keyword (namespace a-key) "syms")

               (and (symbol? a-key)
                (not (qualified-symbol? a-key)))
               (keyword (namespace a-key) "syms")

               :else nil)]

  (when ?local
   (let [new-local (symbol (name a-key))]
    (if (contains? @locals new-local)
     (let [[new-local-indexed new-local-no-index] (local+local-indexed a-key locals)]
      [:via-key [a-key new-local-indexed new-local-no-index]])
     (do
      [:via-vec [?local new-local]]))))))


(defn- val->?destructure-more [a-val]
 (cond
  (and
   (vector? a-val)
   (map? (first a-val))) (with-meta (first a-val) {:map-in-vector? true})
  (map? a-val) a-val
  :else nil))



(defn de
 "Prints the vector part of (let [{:keys []} m]) map destructuring for you.
  Use at the REPL, copy the output, and use in your code.

  Given a map m returns the full recursive destructuring form ready for use in regular Clojure (let ...).
  If a specific key at a specific level of the map cannot be destructured, the destructuring stops there for that key.
  An INFO message will be printed with the specific key that cannot be destructured.
  If it encounters a vector which contains maps, it will destructure the first map in the vector.



  Supported map keys:
   - keywords
   - qualified keywords
   - symbols
   - qualified symbols
   - strings (not all string keys can be destructured, avoid strings)

  Usage:
  ```

  (def m  {:name     :alice
           :favorite {:music   [{:genre :rock}
                                {:genre :trance}]
                      :friends #{:bob :clara}}})
  (de m)
  ;=>
  [{:keys [name favorite]} m
   {:keys [music friends]} favorite
   [{:keys [genre]}] music]

  ```
  "
 ([m]
  (de m {:?symbol 'm}))
 ([m {:keys [?symbol locals] :or {locals (atom {})}}]
  {:pre [(map? m)]}

  (transduce
   (map identity)
   (completing
    (fn [{:keys [locals] :as accum} [a-key-original a-val]]
     (let [[?via [?local new-local new-local-no-index]] (key->?local a-key-original locals)
           ?destructure-more (val->?destructure-more a-val)
           accum'            (condp = ?via
                              :via-vec
                              (update-in accum [:left-side ?local]
                               (fn [?v] (conjv ?v new-local)))

                              :via-key
                              (update accum :left-side
                               (fn [m] (assoc m new-local ?local)))

                              ;else, no change
                              accum)
           accum''           accum'
           local-to-index (or new-local-no-index new-local)]


      (when local-to-index
       (swap! locals update local-to-index (fnil inc 0)))


      (timbre/spy new-local)
      (timbre/spy @locals)
      ;print message if a key cannot be destructured
      (when (nil? ?local)
       (println "INFO ::: Map key" (str "'" a-key-original "'") "does not support destructuring"))


      (if (and ?local ?destructure-more)
       ;'schedule' further destructuring
       (update-in accum'' [:destructure-more] conj [new-local ?destructure-more])
       ;else
       accum'')))

    (fn [{:keys [left-side destructure-more locals] :as accum-final}]
     (let [left-side' (if (:map-in-vector? (meta m)) [left-side] left-side)
           right-side (or ?symbol (symbol "m"))
           output'    [left-side' right-side]
           ret        (reduce
                       (fn [-output' [a-symbol m]]
                        (apply conj -output'
                         (trampoline de m {:?symbol a-symbol :locals locals})))
                       output'
                       destructure-more)]
      ret)))
   ;reduce accum "state"
   {:left-side {} :destructure-more [] :locals locals}
   m)))


(comment
 (let [m1 {:name     :alice
           :favorite {:music   [{:genre :rock}
                                {:genre :trance}]
                      :friends #{:bob :clara}}}

       m2 {'a      {:b [{:hello :world-1} {:hello :world-2}]}
           "music" {:genre "trance"}}]

  (de m1)

  (pprint-let
   (de m2))

  ))
