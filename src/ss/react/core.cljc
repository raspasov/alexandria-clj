(ns ss.react.core)


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

