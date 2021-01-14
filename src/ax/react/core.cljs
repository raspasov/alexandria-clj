(ns ax.react.core
  (:require-macros [ax.react.macros :as macro])
  (:require [react]
            [create-react-class]
            [taoensso.timbre :as timbre]
            [goog.object :as obj]
            [ax.react.state :as state]))


(def ^js/Object React react)


(def use-state (obj/get React (name :useState)))


(def memo (obj/get React (name :memo)))


(defn get-create-react-class [] create-react-class)


(defn- -cljs-props
  "Takes x which is immutable ClojureScript data and wraps it in a #js{} object to be passed to React components"
  [x]
  (if-let [k (get x :key)]
    #js{:cljs x :key k}
    #js{:cljs x}))
(def cljs-props (memoize -cljs-props))


(defn create-element-js
  "Create element for JavaScript components which need mutable JS data to work"
  [component props & children]
  (apply
    react/createElement
    component
    (if (map? props)
      ;convert Clojure data to JS object
      (clj->js props)
      ;else, assume opts is already a JS object
      props)
    children))


(defn create-element-cljs
  "Create element for ClojureScript components with immutable data"
  [component props & children]
  (apply
    react/createElement
    component
    (cljs-props props)
    children))


(defn get-props-class
  "Takes a React component instance and returns the ClojureScript data"
  [this]
  (obj/getValueByKeys this "props" "cljs"))

(defn get-props-func
  "Get props for function components"
  [props]
  (obj/get props "cljs"))


(defn root-component [props]
  (let [[_ root-refresh-hook] (use-state (random-uuid))
        _ (reset! state/*root-refresh-hook root-refresh-hook)
        {:keys [app-view]} (get-props-func props)]
    (app-view @state/*app-state)))
(def root-view-func (partial create-element-cljs root-component))


(defn get-root-view [app-view]
  (root-view-func {:app-view app-view}))
