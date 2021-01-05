(ns ax.react-native.core
  (:require [react]
            [react-native]
            [create-react-class]
            [taoensso.timbre :as timbre]
            [goog.object :as obj]
            [ax.react.core :as r]))


(def ^js/Object ReactNative react-native)


(def ^js/Object AppRegistry (obj/get ReactNative (name :AppRegistry)))


(def view (partial r/create-element-js (obj/get ReactNative (name :View))))


(defn- -get-alert-fn [] (obj/getValueByKeys ReactNative "Alert" "alert"))


(defn alert
  ([title]
   ((-get-alert-fn) title))
  ([title message]
   ((-get-alert-fn) title message #js[]))
  ([title message button-specs]
   ((-get-alert-fn) title message (clj->js button-specs))))


(def safe-area-view (partial r/create-element-js (obj/get ReactNative "SafeAreaView")))


(def button (partial r/create-element-js (obj/get ReactNative "Button")))


(def text (partial r/create-element-js (obj/get ReactNative "Text")))


(def image (partial r/create-element-js (obj/get ReactNative "Image")))


(def touchable-opacity (partial r/create-element-js (obj/get ReactNative "TouchableOpacity")))


(def modal (partial r/create-element-js (obj/get ReactNative "Modal")))


(def activity-indicator (partial r/create-element-js (obj/get ReactNative "ActivityIndicator")))


(def ^js/Object status-bar (obj/get ReactNative "StatusBar"))


(def ^js/Object pixel-ratio (obj/get ReactNative "PixelRatio"))


(def ^js/Object virtualized-list (partial r/create-element-js (obj/get ReactNative (name :VirtualizedList))))


(def scroll-view (partial r/create-element-js (obj/get ReactNative (name :ScrollView))))


(def keyboard-avoiding-view (partial r/create-element-js (obj/get ReactNative (name :KeyboardAvoidingView))))


(def text-input (partial r/create-element-js (obj/get ReactNative (name :TextInput))))


(def platform (obj/getValueByKeys ReactNative (name :Platform) (name :OS)))


(defn ios? []
  (= "ios" platform))


(defn android? []
  (= "android" platform))

;Animated
;----------------------------------------------------------------------------------------------------------------------
(def animated-view (partial r/create-element-js (obj/getValueByKeys ReactNative (name :Animated) (name :View))))


(defn remove-yellow-box
  "Remove yellow box warning after 2 sec for convenience"
  []
  ((obj/getValueByKeys ReactNative "YellowBox" "ignoreWarnings") #js["Feature :formatters"
                                                                     "console"]))

(def dev? js/__DEV__)


(def prod? (not dev?))

