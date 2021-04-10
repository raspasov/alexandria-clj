(ns ax.react.core)


(defmacro e
 "Creates a React element with displayName"
 [component]
 (let [fully-qualified-name# (str *ns* "/" component)]
  `(partial
    ax.react.core/create-element-cljs
    (ax.react.core/memo
     (ax.cljs.googc/assoc-obj! ~component "displayName" ~fully-qualified-name#)))))


(defmacro with-keys
 "Takes React elements and adds :key to each one at compile time."
 [& elements]
 (vec
  (map-indexed
   (fn [idx# element#]
    (apply list
     (map-indexed
      (fn [idx-2# m#]
       (if (= 1 idx-2#)
        (assoc m# :key idx#)
        m#))
      element#)))
   elements)))

