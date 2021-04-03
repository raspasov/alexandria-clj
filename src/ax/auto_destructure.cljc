(ns ax.auto-destructure)


(defn auto-destructure
  "Given a map m returns the full recursive destructuring form ready for use in regular Clojure.
   If it encounters a vector which contains maps, it will destructure the first map in the vector.
   At the moment supports only keywords and qualified keywords. No strings, symbols, etc as map keys.

   Usage:
   ```
   (auto-destructure
      {:name     :alice
       :favorite {:music   [{:genre :rock}
                            {:genre :trance}]
                  :friends #{:bob :clara}}})

   ;=>
   [{:keys [name favorite]} m
    {:keys [music friends]} favorite
    [{:keys [genre]}] music]
   ```
   "
  ([m]
   (auto-destructure [] ['m m]))
  ([output [?symbol m]]
   (transduce
     (map identity)
     (completing
       (fn [accum [a-key a-val]]
         (let [[a-symbol accum']
               (if (qualified-keyword? a-key)
                 (let [k-ns      (namespace a-key)
                       some-keys (keyword k-ns "keys")]

                   [(symbol (name a-key))
                    (update-in
                      accum [:form some-keys]
                      (fn [?v] ((fnil conj []) ?v (symbol (name a-key)))))])

                 [(symbol (name a-key))
                  (update-in accum [:form (keyword "keys")]
                             (fn [?v] ((fnil conj []) ?v (symbol (name a-key)))))])]


           (cond
             (and (vector? a-val)
                  (map? (first a-val)))

             (update-in accum' [:destruct-more] conj [a-symbol (with-meta
                                                                 (first a-val)
                                                                 {:first-map-in-vector? true})])

             (map? a-val)
             (update-in accum' [:destruct-more] conj [a-symbol a-val])

             :else accum')))
       (fn [{:keys [form destruct-more] :as accum-final}]
         (let [left-side  (if (:first-map-in-vector? (meta m)) [form] form)
               right-side (if ?symbol ?symbol (symbol "m"))
               output'    (conj output left-side right-side)]

           (if (seq destruct-more)
             (reduce
               (fn [-output' [a-symbol m]]
                 (apply conj -output' (trampoline auto-destructure [] [a-symbol m])))
               output'
               destruct-more)
             ;else
             output')
           )))
     {:form nil :destruct-more []}
     m)))

(comment
  (auto-destructure
    {:name     :alice
     :favorite {:music   [{:genre :rock}
                          {:genre :trance}]
                :friends #{:bob :clara}}}))
