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


;Basic
;---------------------------------------------------------------------------------
;(defn basic-root-component
;  "Usage:
;
;    (basic-root-view {:app-view app-view})
;
;  "
;  [props]
;  (let [[_ root-refresh-hook] (use-state (random-uuid))
;        _ (reset! state/*root-refresh-hook root-refresh-hook)
;        {:keys [app-view]} (props-fnc props)]
;    (app-view @state/*app-state)))
;(def basic-root-view (e basic-root-component))


;Advanced
;---------------------------------------------------------------------------------
;(defn advanced-root-component
;  "Usage:
;
;   (advanced-root-view
;    {:app-view         app-view
;     :*datascript-conn *datascript-conn
;     :*app-state       *app-state
;     :app-state-fn     app-state-fn})
;
;   "
;  [props]
;  (let [[_ root-refresh-hook] (use-state (random-uuid))
;        _ (reset! state/*root-refresh-hook root-refresh-hook)
;        {:keys [app-view *app-state app-state-fn]} (props-fnc props)]
;    (app-view (app-state-fn @*app-state))))
;(def advanced-root-view (e advanced-root-component))
;
;
