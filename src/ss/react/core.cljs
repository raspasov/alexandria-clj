(ns ss.react.core
 (:require
  [ss.react.state :as state]
  [cljs-bean.core :as b]
  [create-react-class]
  [goog.object :as obj]
  [react]
  [taoensso.timbre :as timbre]
  [ss.react.state-fns :as ax|state-fns]
  [ss.cljs.googc :as ax|goog])
 (:require-macros [ss.react.core]))


(def ^js/Object React react)


(def use-state (.-useState React))


(def use-ref (.-useRef React))


(defn current [^js/Object ref]
 (.-current ref))


(declare use-effect)


(def use-effect (.-useEffect React))


(defn use-effect-once
 "useEffect with empty #js[]
  Similar to componentDidMount/componentWillUnmount"
 [f]
 (use-effect f #js[]))


(defn use-swap
 "Acts like (swap! ...).
  Instead of set-x returns a function swap-x.
  swap-x is a function of one argument. Takes a fn which will be called with the current x.
  The result of swap-x is then used to set-x."
 [x]
 (let [[x set-x] (use-state x)
       swap-x (fn [f] (set-x (f x)))]
  [x swap-x]))


(defn native-event
 "Extracts the nativeEvent from a React SyntheticEvent."
 [^js/Object synthetic-event]
 (b/->clj (.-nativeEvent synthetic-event)))


(defn use-refresh []
 (let [[?uuid refresh-hook] (use-state (random-uuid))
       _ (use-effect-once
          (fn []
           (timbre/info "add: auto-refresh-hook")
           (ax|state-fns/update-mutable! :auto-refresh-hooks
            (fn [?set] ((fnil conj #{}) ?set refresh-hook)))

           (fn cleanup []
            (ax|state-fns/update-mutable! :auto-refresh-hooks
             (fn [?set] ((fnil disj #{}) ?set refresh-hook)))
            (timbre/info "cleanup: auto-refresh-hook"))))]
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
  #js{:_cljs x :key k}
  #js{:_cljs x}))
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
 (.. this -props -_cljs))


(defn props
 "Get props for function components"
 [^js/Object props]
 (.-_cljs props))


(defn children
 "Get the component children"
 [^js/Object props]
 (.-children props))


(defn ^js/Object use-mounted-obj
 "Returns a (mutable) mounted-obj which can used to check if the component is currently mounted."
 []
 (let [^js/Object mounted-obj (use-ref false)]
  (use-effect-once
   (fn []
    (set! (.-current mounted-obj) true)
    (fn cleanup []
     (set! (.-current mounted-obj) false))))
  mounted-obj))


(defn mounted? [^js/Object mounted-obj]
 (true? (.-current mounted-obj)))


(defn use-prop-hook
 "'Convert' a prop value to a local state value. To be used for performance reasons
  to avoid re-rendering from the root."
 [path-or-value default]
 (let [[x hook-f] (use-state default)
       mounted-obj (use-mounted-obj)]
  (if (vector? path-or-value)
   (do
    (ax|state-fns/set-mutable!
     path-or-value
     (fn [f]
      (let [new-x (f x)]
       (if (mounted? mounted-obj)
        (hook-f new-x)
        (do
         (timbre/warn "Component not mounted" path-or-value new-x)))
       ;return the new value
       new-x)))
    ;return
    x)
   ;else, return
   path-or-value)))
