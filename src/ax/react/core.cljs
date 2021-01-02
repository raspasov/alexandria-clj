(ns ax.react.core
  (:require [react]
            [react-native]
            [create-react-class]
            [taoensso.timbre :as timbre]
            [goog.object :as obj]))


(defn -get-in [obj ks]
  (apply obj/getValueByKeys obj (mapv name ks)))


(def ^js/Object React react)


(def ^js/Object ReactNative react-native)


(def ^js/Object AppRegistry (obj/get ReactNative (name :AppRegistry)))


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


(def view (partial create-element-js (obj/get ReactNative (name :View))))


(defn- -get-alert-fn [] (obj/getValueByKeys ReactNative "Alert" "alert"))
(defn alert
  ([title]
   ((-get-alert-fn) title))
  ([title message]
   ((-get-alert-fn) title message #js[]))
  ([title message button-specs]
   ((-get-alert-fn) title message (clj->js button-specs))))


(def safe-area-view (partial create-element-js (obj/get ReactNative "SafeAreaView")))


(def button (partial create-element-js (obj/get ReactNative "Button")))


(def text (partial create-element-js (obj/get ReactNative "Text")))


(def image (partial create-element-js (obj/get ReactNative "Image")))


(def touchable-opacity (partial create-element-js (obj/get ReactNative "TouchableOpacity")))


(def modal (partial create-element-js (obj/get ReactNative "Modal")))


(def activity-indicator (partial create-element-js (obj/get ReactNative "ActivityIndicator")))


(def dimensions (.-Dimensions ReactNative))


(defn get-dimensions [] (js->clj (.get dimensions "window") :keywordize-keys true))


(def ^js/Object status-bar (obj/get ReactNative "StatusBar"))


(def ^js/Object pixel-ratio (obj/get ReactNative "PixelRatio"))


(def ^js/Object virtualized-list (partial create-element-js (obj/get ReactNative (name :VirtualizedList))))


(def scroll-view (partial create-element-js (obj/get ReactNative (name :ScrollView))))


(def keyboard-avoiding-view (partial create-element-js (obj/get ReactNative (name :KeyboardAvoidingView))))


(def text-input (partial create-element-js (obj/get ReactNative (name :TextInput))))


(def platform (-get-in ReactNative [:Platform :OS]))


(defn ios? []
  (= "ios" platform))


(defn android? []
  (= "android" platform))

;Animated
;----------------------------------------------------------------------------------------------------------------------
(def animated-view (partial create-element-js (obj/getValueByKeys ReactNative (name :Animated) (name :View))))

(defn get-props
  "Takes a React component instance and returns the ClojureScript data"
  [this]
  (obj/getValueByKeys this "props" "cljs"))

(defn get-props-fn
  "Get props for function components"
  [props]
  (obj/get props "cljs"))


(defn get-root-props [this]
  @(obj/getValueByKeys this "props" "cljs"))


(defn remove-yellow-box
  "Remove yellow box warning after 2 sec for convenience"
  []
  ((obj/getValueByKeys ReactNative "YellowBox" "ignoreWarnings") #js["Feature :formatters"
                                                                     "console"]))


(def dev? js/__DEV__)


(def prod? (not dev?))

