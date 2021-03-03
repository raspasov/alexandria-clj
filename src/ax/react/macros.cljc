(ns ax.react.macros
  #?(:cljs
     (:require-macros ax.react.macros)))


(defmacro create-react-class [& params]
  `((ax.react.core/get-create-react-class)
    (cljs.core/js-obj
      "shouldComponentUpdate" (fn [next-props# next-state#]
                                (cljs.core/this-as this#
                                  (let [next-props-cljs# (js/goog.object.get next-props# "cljs")
                                        props-cljs#      (-> this#
                                                             (js/goog.object.get "props")
                                                             (js/goog.object.get "cljs"))]
                                    (if (cljs.core/identical? next-props-cljs# props-cljs#)
                                      false
                                      true))))

      ~@(mapv
          (fn [x]
            (if (keyword? x)
              (name x)
              x)) params))))


;usage
(comment
  (def a-class
    (macro/create-react-class
      :render
      #(this-as this
         (let []))))
  (def a-view (partial r/create-element a-class)))


(defmacro js-directive
  [directive]
  (list 'js* (str "'" directive "'")))










