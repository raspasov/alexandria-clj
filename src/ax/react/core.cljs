(ns ax.react.core
  (:require [react]
            [create-react-class]
            [taoensso.timbre :as timbre]
            [goog.object :as obj]
            [ax.react.state :as state]))


(def ^js/Object React react)


(def use-state (.-useState React))


(def use-effect (.-useEffect React))


(def memo (.-memo React))


(defn get-create-react-class [] create-react-class)


(defn- -cljs-props
  "Takes x which is immutable ClojureScript data and wraps it in a #js{} object to be passed to React components"
  [x]
  (if-let [k (get x :key)]
    #js{:cljs x :key k}
    #js{:cljs x}))
(def cljs-props (memoize -cljs-props))


(def clj->js-memo (memoize clj->js))


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

;experimental, trying to make views pure; doesn't work so far
(defn create-element-js-2
  "Create element for JavaScript components which need mutable JS data to work"
  [component props & children]
  (timbre/info "create-element-js-2 ...")
  (apply
    react/createElement
    component
    (if (map? props)
      ;convert Clojure data to JS object
      (clj->js-memo props)
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
  [^js/Object this]
  (.. this -props -cljs))

(defn get-props-func
  "Get props for function components"
  [^js/Object props]
  (.-cljs props))


;Basic
;---------------------------------------------------------------------------------
(defn basic-root-component [props]
  (let [[_ root-refresh-hook] (use-state (random-uuid))
        _ (reset! state/*root-refresh-hook root-refresh-hook)
        {:keys [app-view]} (get-props-func props)]
    (app-view @state/*app-state)))
(def basic-root-view-func (partial create-element-cljs basic-root-component))


(defn basic-root-view [app-view]
  (basic-root-view-func {:app-view app-view}))


;Advanced
;---------------------------------------------------------------------------------
(defn advanced-root-component [props]
  (let [[_ root-refresh-hook] (use-state (random-uuid))
        _ (reset! state/*root-refresh-hook root-refresh-hook)
        {:keys [app-view *datascript-conn *app-state app-state-fn]} (get-props-func props)]
    (app-view (app-state-fn *datascript-conn @*app-state))))
(def advanced-root-view-func (partial create-element-cljs (memo advanced-root-component)))


(defn advanced-root-view [app-view *datascript-conn *app-state app-state-fn]
  (advanced-root-view-func
    {:app-view         app-view
     :*datascript-conn *datascript-conn
     :*app-state       *app-state
     :app-state-fn     app-state-fn}))

