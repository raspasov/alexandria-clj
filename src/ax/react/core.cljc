(ns ax.react.core)


(defmacro e
  "Creates a React element with displayName"
  [component]
  (let [fully-qualified-name# (str *ns* "/" component)]
    `(partial
      ax.react.core/create-element-cljs
      (ax.react.core/memo
        (ax.cljs.googc/assoc-obj! ~component "displayName" ~fully-qualified-name#)))))

;`(ax.cljs.googc/assoc-obj! ~component "displayName" ~fully-qualified-name#)
