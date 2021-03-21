(ns ax.react-native.core
  (:require [react]
            [react-native]
            [taoensso.timbre :as timbre]
            [goog.object :as obj]
            [ax.react.core :as rc]
            [cljs-bean.core :as b]))


(def ^js/Object ReactNative react-native)

(def ^js/Object AppRegistry (.-AppRegistry ReactNative))

(def runAfterInteractions (.. ReactNative -InteractionManager -runAfterInteractions))


(defn- -get-alert-fn [] (.. ReactNative -Alert -alert))


(defn alert
  ([title]
   ((-get-alert-fn) title))
  ([title message]
   ((-get-alert-fn) title message #js[]))
  ([title message button-specs]
   ((-get-alert-fn) title message (b/->js button-specs))))


(def view (partial rc/create-element-js (.-View ReactNative)))


(def safe-area-view (partial rc/create-element-js (.-SafeAreaView ReactNative)))


(def button (partial rc/create-element-js (.-Button ReactNative)))


(def text (partial rc/create-element-js (.-Text ReactNative)))


(def image (partial rc/create-element-js (.-Image ReactNative)))


(def touchable-opacity (partial rc/create-element-js (.-TouchableOpacity ReactNative)))


(def modal (partial rc/create-element-js (.-Modal ReactNative)))


(def activity-indicator (partial rc/create-element-js (.-ActivityIndicator ReactNative)))


(def ^js/Object status-bar (.-StatusBar ReactNative))


(def ^js/Object pixel-ratio (.-PixelRatio ReactNative))


(def ^js/Object virtualized-list (partial rc/create-element-js (.-VirtualizedList ReactNative)))


(def scroll-view (partial rc/create-element-js (.-ScrollView ReactNative)))


(def keyboard-avoiding-view (partial rc/create-element-js (.-KeyboardAvoidingView ReactNative)))


(def text-input (partial rc/create-element-js (.-TextInput ReactNative)))


(def platform (.. ReactNative -Platform -OS))


(defn ios? []
  (= "ios" platform))


(defn android? []
  (= "android" platform))


(defn web? []
  (= "web" platform))


;Animated
;----------------------------------------------------------------------------------------------------------------------
(def animated-view (partial rc/create-element-js (.. ReactNative -Animated -View)))


(defn remove-yellow-box
  "Remove yellow box warning after 2 sec for convenience"
  []
  (let [f (.. ReactNative -LogBox -ignoreAllLogs)]
    (f))
  ;((.. ReactNative -YellowBox -ignoreWarnings) #js["Feature :formatters" "console"])
  )


(def dev? js/window.__DEV__)


(def prod? (not dev?))

