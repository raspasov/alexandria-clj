(ns ss.react.core
 (:require
  [cljs-bean.core :as b]
  [create-react-class]
  [react]
  [ss.cljs.gg]                            ;DO NOT REMOVE, used by core.cljc for macros
  [ss.react.state-fns :as ss.stf]
  [taoensso.timbre :as timbre])
 (:require-macros [ss.react.core]))


(def ^js/Object React react)


(def children-count (.. React -Children -count))


(def use-state (.-useState React))


(def use-ref (.-useRef React))


(defn current [^js ref]
 (.-current ref))


(def use-effect (.-useEffect React))


(def use-callback (.-useCallback React))
;
;// Hook
;function usePrevious(value) {
;                             // The ref object is a generic container whose current property is mutable ...
;                             // ... and can hold any value, similar to an instance property on a class
;                             const ref = useRef();
;                             // Store current value in ref
;                             useEffect(() => {
;                                              ref.current = value;
;                                              }, [value]); // Only re-run if value changes
;                             // Return previous value (happens before update in useEffect above)
;                             return ref.current;
;                             }

(defn use-previous [x]
 (let [^js ref (use-ref)
       _   (use-effect
            (fn []
             (set! (.-current ref) x)
             (fn cleanup []))
            ;only re-run if 'x' changes
            #js[x])]
  ;return the current 'x'
  (.-current ref)))

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


(defn use-atom
 "Shared mutable state. Use with caution."
 [*x]
 (let [[*x _] (use-state *x)]
  *x))


(defn native-event
 "Extracts the nativeEvent from a React SyntheticEvent."
 [^js/Object synthetic-event]
 (b/->clj (.-nativeEvent synthetic-event)))


(defn use-refresh []
 (let [[?uuid refresh-hook] (use-state (random-uuid))
       _ (use-effect-once
          (fn []
           (timbre/info "add: auto-refresh-hook")
           (ss.stf/update-mutable! :auto-refresh-hooks
            (fn [?set] ((fnil conj #{}) ?set refresh-hook)))

           (fn cleanup []
            (ss.stf/update-mutable! :auto-refresh-hooks
             (fn [?set] ((fnil disj #{}) ?set refresh-hook)))
            (timbre/info "cleanup: auto-refresh-hook"))))]
  ?uuid))


(defn refresh! []
 (run!
  (fn [f]
   (f nil)
   (f (random-uuid)))
  (ss.stf/get-mutable :auto-refresh-hooks)))


(def memo (.-memo React))


(defn get-create-react-class [] create-react-class)


(defn- -cljs-props
 "Takes x which is immutable ClojureScript data and wraps it in a #js{} object to be passed to React components"
 [x]
 (if-let [k (get x :key)]
  #js{:_cljs x :key k}
  #js{:_cljs x}))
(def cljs-props -cljs-props)


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


(defn are-equal? [prev-props next-props]
 (let [equal?
       (=
        (ss.cljs.gg/get-obj prev-props :_cljs)
        (ss.cljs.gg/get-obj next-props :_cljs))]
  equal?))


(defn props-class
 "Get props for class components"
 [^js/Object this]
 (.. this -props -_cljs))


(defn props
 "Get props for function components"
 [^js/Object props]
 (.-_cljs props))


(defn children
 "IMPORTANT! Needs to be called on the 'original' React props, not CLJS props.
  Get the component children"
 [^js/Object props]
 (.-children props))


(defn ^js/Object use-mounted-obj
 "Returns a (mutable) mounted-obj which can be used to check if the component is currently mounted."
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
 (let [[x set-x] (use-state default)
       mounted-obj (use-mounted-obj)]
  (if (vector? path-or-value)
   (do
    (ss.stf/set-mutable!
     path-or-value
     (fn [f]
      (let [new-x (f x)]
       (if (mounted? mounted-obj)
        (set-x new-x)
        (timbre/warn "Component not mounted" path-or-value new-x))
       ;return the new value
       new-x)))
    ;return
    x)
   ;else, return
   path-or-value)))


(defn swap-hook! [k f]
 (let [path    [:hooks k]
       ?hook-f (ss.stf/get-mutable [:hooks k])]
  (if (fn? ?hook-f)
   (?hook-f f)
   (timbre/warn "No hook found at path" path))))


(defn hook-state
 "WARNING: If you call this function right after a swap-hook!, it might erase the update."
 []
 (into
  {}
  (map (fn [[k f]] [k (f identity)]))
  (ss.stf/get-mutable [:hooks])))
