(ns ax.auto-destructure)


(def conjv (fnil conj []))


(defn auto-destructure-let
  "Prints (let [{:keys []} m]) map destructuring for you. Copy the output, and use in your code.

   Given a map m returns the full recursive destructuring form ready for use in regular Clojure (let ...).
   If it encounters a vector which contains maps, it will destructure the first map in the vector.
   At the moment supports only keywords and qualified keywords. No strings, symbols, etc as map keys.

   Usage:
   ```

   (def m  {:name     :alice
            :favorite {:music   [{:genre :rock}
                                 {:genre :trance}]
                       :friends #{:bob :clara}}})
   (auto-destructure m)
   ;=>
   [{:keys [name favorite]} m
    {:keys [music friends]} favorite
    [{:keys [genre]}] music]

   ```
   "
  ([m]
   (auto-destructure-let m {:?symbol 'm :pretty? true}))
  ([m {:keys [?symbol pretty?]}]

   (transduce
     (map identity)
     (completing
       (fn [accum [a-key a-val]]
         (let [a-symbol       (symbol (name a-key))
               path           (if (qualified-keyword? a-key)
                                [:form (keyword (namespace a-key) "keys")]
                                [:form (keyword "keys")])
               accum'         (update-in
                                accum path
                                (fn [?v] ((fnil conj []) ?v (symbol (name a-key)))))
               ?destruct-more (cond
                                (and (vector? a-val)
                                     (map? (first a-val))) (with-meta (first a-val) {:map-in-vector? true})
                                (map? a-val) a-val
                                :else nil)]
           (if ?destruct-more
             ;'schedule' further destructuring
             (update-in accum' [:destruct-more] conj [a-symbol ?destruct-more])
             ;else
             accum')))
       (fn [{:keys [form destruct-more] :as accum-final}]
         (let [left-side  (if (:map-in-vector? (meta m)) [form] form)
               right-side (if ?symbol ?symbol (symbol "m"))
               output'    (conj [] left-side right-side)
               ret        (reduce
                            (fn [-output' [a-symbol m]]
                              (apply conj -output' (trampoline auto-destructure-let m {:?symbol a-symbol})))
                            output'
                            destruct-more)]

           ret)))
     {:form nil :destruct-more []}
     m)))

(comment
  (let [m {:name     :alice
           :favorite {:music   [{:genre :rock}
                                {:genre :trance}]
                      :friends #{:bob :clara}}}]

    (auto-destructure-let m)))
