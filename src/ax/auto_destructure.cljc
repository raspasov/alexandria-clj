(ns ax.auto-destructure)


(def conjv (fnil conj []))

(defn has-empty-space? [s]
  (boolean (some #(= " " %) (seq s))))


(defn can-destructure-string?
  "Naively try to determine if string can be destructured.
   Incomplete."
  [s]
  (boolean
    (and
      ;check if not empty
      (seq s)
      ;ensure no empty space
      (not (some #(= " " %) (seq s))))))


(defn print-pretty-let [v-for-let]
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


(defn- key->?destructure-key [a-key]
  (cond
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

    :else nil))


(defn- val->?destructure-more [a-val]
  (cond
    (and
      (vector? a-val)
      (map? (first a-val))) (with-meta (first a-val) {:map-in-vector? true})
    (map? a-val) a-val
    :else nil))


(defn auto-destructure-let
  "Prints (let [{:keys []} m]) map destructuring for you. Copy the output, and use in your code.

   Given a map m returns the full recursive destructuring form ready for use in regular Clojure (let ...).
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
   (auto-destructure-let m)
   ;=>
   [{:keys [name favorite]} m
    {:keys [music friends]} favorite
    [{:keys [genre]}] music]

   ```
   "
  ([m]
   (auto-destructure-let m {:?symbol 'm}))
  ([m {:keys [?symbol]}]
   {:pre [(map? m)]}

   (transduce
     (map (fn [[a-key-original a-val]]
            ;transform map entry
            [[a-key-original (key->?destructure-key a-key-original)] (val->?destructure-more a-val)]))
     (completing
       (fn [accum [[a-key-original ?destructure-key] ?destructure-more]]
         (let [?path  (when ?destructure-key [:left-side ?destructure-key])
               accum' (if ?path
                        (update-in accum ?path
                          (fn [?v] (conjv ?v (symbol (name a-key-original)))))
                        accum)]

           ;print message if a key cannot be destructured
           (when (nil? ?path)
             (println "INFO ::: Map key" (str "'" a-key-original "'") "does not support destructuring"))

           (if (and ?path ?destructure-more)
             ;'schedule' further destructuring
             (update-in accum' [:destructure-more] conj [(symbol (name a-key-original)) ?destructure-more])
             ;else
             accum')))
       (fn [{:keys [left-side destructure-more] :as accum-final}]
         (let [left-side' (if (:map-in-vector? (meta m)) [left-side] left-side)
               right-side (or ?symbol (symbol "m"))
               output'    [left-side' right-side]
               ret        (reduce
                            (fn [-output' [a-symbol m]]
                              (apply conj -output' (trampoline auto-destructure-let m {:?symbol a-symbol})))
                            output'
                            destructure-more)]

           ret)))
     {:left-side {} :destructure-more []}
     m)))


(comment
  (let [m1 {:name     :alice
            :favorite {:music   [{:genre :rock}
                                 {:genre :trance}]
                       :friends #{:bob :clara}}}

        m2 {'a      {:b [{:hello :world-1} {:hello :world-2}]}
            "music" {:genre "trance"}}]

    (auto-destructure-let m1)
    (auto-destructure-let m2)))
