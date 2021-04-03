(ns ax.react.core
  (:require
    [ax.react.state :as state]
    [cljs-bean.core :as b]
    [create-react-class]
    [goog.object :as obj]
    [react]
    [taoensso.timbre :as timbre]
    [ax.react.state-fns :as ax|state-fns]
    [ax.cljs.googc :as ax|goog])
  (:require-macros [ax.react.core]))


(def ^js/Object React react)


(def use-state (.-useState React))


(def use-ref (.-useRef React))


(declare use-effect)


(def use-effect (.-useEffect React))


(defn use-refresh []
  (let [[?uuid refresh-hook] (use-state (random-uuid))
        _ (use-effect
            (fn []
              (timbre/info "add: auto-refresh-hook")
              (ax|state-fns/update-mutable! :auto-refresh-hooks
                (fn [?set] ((fnil conj #{}) ?set refresh-hook)))

              (fn cleanup []
                (ax|state-fns/update-mutable! :auto-refresh-hooks
                  (fn [?set] ((fnil disj #{}) ?set refresh-hook)))
                (timbre/info "cleanup: auto-refresh-hook")))
            #js[])]
    ?uuid))


(defn refresh! []
  (run!
    (fn [f]
      (f nil)
      (f (random-uuid)))
    (ax|state-fns/get-mutable :auto-refresh-hooks)))


(def memo (.-memo React))


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
      (b/->js props)
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


(defn props-class
  "Get props for class components"
  [^js/Object this]
  (.. this -props -cljs))


(defn props-fnc
  "Get props for function components"
  [^js/Object props]
  (.-cljs props))


(defn ^js/Object use-mounted-ref []
  (let [^js/Object mounted (use-ref false)]
    (use-effect
      (fn []
        (set! (.-current mounted) true)
        (fn cleanup []
          (set! (.-current mounted) false)))
      #js[])
    mounted))


(defn prop->hook
  "Convert a prop value to a local state value. To be used for performance reasons
   to avoid re-rendering from the root."
  [path-or-value default]
  (let [[x hook-f] (use-state default)
        mounted-obj (use-mounted-ref)]

    (if (vector? path-or-value)
      (do
        (ax|state-fns/set-mutable!
          path-or-value
          (fn [f]
            (let [new-x (f x)]
              (if (true? (.-current mounted-obj))
                (hook-f new-x)
                (do
                  (timbre/warn "Component not mounted" path-or-value new-x)))
              ;return the new value
              new-x)))
        ;return
        x)
      ;else, return
      path-or-value)))
