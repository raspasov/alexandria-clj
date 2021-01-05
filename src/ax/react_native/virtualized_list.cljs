(ns ax.react-native.virtualized-list
  (:require-macros [ax.react.macros :as macro])
  (:require [react]
            [cljs.core.async :refer [go chan >! <! put! timeout]]
            [ax.react.core :as rc]
            [ax.react-native.core :as r]
            [goog.object :as obj]
            [ax.react.state-fns :as state-fns]
            [applied-science.js-interop :as j]
            [taoensso.timbre :as timbre]))


(def immutable-list-class
  (macro/create-react-class
    :render
    #(this-as this
       (let [{:keys [data ref-key] :as props} (rc/get-props-class this)
             props-no-data (dissoc props :data)]
         (r/virtualized-list
           (-> {:getItem             (fn [data idx]
                                       ;return tuple [item-data idx] - idx needed for keyExtractor
                                       ;item-data is Clojure data (usually a map)
                                       (let [item-data (nth data idx nil)]
                                         [item-data idx]))
                :getItemCount        (fn [data] (count data))
                ;needs to return a key as string
                :keyExtractor        (fn [[_ idx]] (str idx))
                :ref                 (fn [ref]
                                       (when ref-key
                                         ;(timbre/info "saving ref..." ref-key)
                                         ;(timbre/spy ref)
                                         (state-fns/set-mutable! [:refs ref-key] ref)))
                :initialNumToRender  2
                :windowSize          2
                :renderItem          (fn [^js/Object js-object]
                                       (let [[item-data _] (.-item js-object)
                                             idx         (.-index js-object)
                                             render-item (:render-item props)]
                                         (render-item item-data idx)))
                :scrollEventThrottle 1}
               ;merge with props (without data)
               (merge props-no-data)
               ;convert to JS
               (clj->js)
               ;add back immutable data to the JS object
               (j/assoc! :data data)))))))
(def immutable-list-view (partial rc/create-element-cljs immutable-list-class))

