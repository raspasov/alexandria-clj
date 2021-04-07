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


(defn local-n+local-orig
 [a-key locals-index]
 (let [local-orig (symbol
                   (clojure.string/join "-"
                    (filter string?
                     [(namespace a-key) (name a-key)])))
       local-n    (if-let [[new-local idx] (find @locals-index local-orig)]
                   (symbol (str new-local "-" idx))
                   local-orig)]
  [local-n local-orig]))


(defn- key->?local [a-key locals-index]
 (let [?keys-destruct
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

        :else nil)]
  (when ?keys-destruct
   (let [local-name-only (symbol (name a-key))]
    (if (contains? @locals-index local-name-only)
     (let [[local-n local-orig] (local-n+local-orig a-key locals-index)]
      [:via-orig-key
       {:local-to-index local-orig
        :new-local      local-n}])
     (let []
      [:via-dest-vec
       {:local-to-index local-name-only
        :new-local      local-name-only
        :keys-destruct  ?keys-destruct}]))))))


(defn- val->?destructure-more [a-val]
 (cond
  (and
   (vector? a-val)
   (map? (first a-val))) (with-meta (first a-val) {:map-in-vector? true})
  (map? a-val) a-val
  :else nil))

(defonce *a (atom nil))

(defn prepare-a-key
 "Special case for symbols destructuring like {a (quote a)}"
 [a-key]
 (if (symbol? a-key)
  `'~a-key
  a-key))

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
  (de m {:?symbol nil}))
 ([m {:keys [?symbol locals-index] :or {locals-index (atom {})}}]
  {:pre [(map? m)]}
  (transduce
   (map identity)
   (completing
    (fn [{:keys [locals-index] :as accum} [a-key a-val]]
     (let [[?via {:keys [local-to-index new-local keys-destruct]}] (key->?local a-key locals-index)
           ?destructure-more (val->?destructure-more a-val)
           accum'            (condp = ?via
                              ;choose type of destructuring

                              :via-dest-vec
                              ;{:keys [] :strs []} style
                              (update-in accum [:left-side keys-destruct]
                               (fn [?v] (conjv ?v new-local)))

                              :via-orig-key
                              ;{xyz :xyz} style
                              (update accum :left-side
                               (fn [m] (assoc m new-local (prepare-a-key a-key))))

                              ;else, no change
                              accum)]


      (when local-to-index
       (swap! locals-index update local-to-index (fnil inc 1))

       (reset! *a locals-index))

      ;print message if a key cannot be destructured
      (when (nil? ?via)
       (println "INFO ::: Map key" (str "'" a-key "'") "does not support destructuring"))


      (if (and ?via ?destructure-more)
       ;'schedule' further destructuring
       (update-in accum' [:destructure-more] conj [new-local ?destructure-more])
       ;else
       (do
        (timbre/info "READY")
        accum'))))

    (fn [{:keys [left-side destructure-more locals-index] :as accum-final}]
     (let [left-side' (if (:map-in-vector? (meta m))
                       [left-side]
                       left-side)
           right-side (or ?symbol (symbol "your-map"))
           output'    [left-side' right-side]
           ret        (reduce
                       (fn [-output' [a-symbol m]]
                        (apply conj -output'
                         (trampoline de m {:?symbol a-symbol :locals-index locals-index})))
                       output'
                       destructure-more)]

      ret)))
   ;reduce accum "state"
   {:left-side {} :destructure-more [] :locals-index locals-index}
   m)))


(comment
 (let [m1 {:name     :alice
           :favorite {:music   [{:genre :rock}
                                {:genre :trance}]
                      :friends #{:bob :clara}}}

       m2 {'a      {:b [{:hello :world-1
                         :b     42}]}
           "music" {:genre "trance"}}]

  (pprint-let
   (de m1))

  (pprint-let
   (de m2))

  ))

