(ns ax.react-native.core
  (:require [react]
            [react-native]
            [taoensso.timbre :as timbre]
            [goog.object :as obj]
            [ax.react.core :as rc]))


(def ^js/Object ReactNative react-native)
(def View (obj/get ReactNative "View"))

(def ^js/Object AppRegistry (obj/get ReactNative "AppRegistry"))


(def view (partial rc/create-element-js (obj/get ReactNative "View")))


;experimental, trying to make views pure; doesn't work so far
(def view-2 (partial rc/create-element-js-2 (rc/memo View (fn [prev-props props]
                                                            (let [ret false]
                                                              (timbre/spy prev-props)
                                                              (timbre/spy props)
                                                              (timbre/spy ret))))))


(defn- -get-alert-fn [] (obj/getValueByKeys ReactNative "Alert" "alert"))


(defn alert
  ([title]
   ((-get-alert-fn) title))
  ([title message]
   ((-get-alert-fn) title message #js[]))
  ([title message button-specs]
   ((-get-alert-fn) title message (clj->js button-specs))))


(def safe-area-view (partial rc/create-element-js (obj/get ReactNative "SafeAreaView")))


(def button (partial rc/create-element-js (obj/get ReactNative "Button")))


(def text (partial rc/create-element-js (obj/get ReactNative "Text")))


(def image (partial rc/create-element-js (obj/get ReactNative "Image")))


(def touchable-opacity (partial rc/create-element-js (obj/get ReactNative "TouchableOpacity")))


(def modal (partial rc/create-element-js (obj/get ReactNative "Modal")))


(def activity-indicator (partial rc/create-element-js (obj/get ReactNative "ActivityIndicator")))


(def ^js/Object status-bar (obj/get ReactNative "StatusBar"))


(def ^js/Object pixel-ratio (obj/get ReactNative "PixelRatio"))


(def ^js/Object virtualized-list (partial rc/create-element-js (obj/get ReactNative (name :VirtualizedList))))


(def scroll-view (partial rc/create-element-js (obj/get ReactNative (name :ScrollView))))


(def keyboard-avoiding-view (partial rc/create-element-js (obj/get ReactNative (name :KeyboardAvoidingView))))


(def text-input (partial rc/create-element-js (obj/get ReactNative (name :TextInput))))


(def platform (obj/getValueByKeys ReactNative (name :Platform) (name :OS)))


(defn ios? []
  (= "ios" platform))


(defn android? []
  (= "android" platform))


(defn web? []
  (= "web" platform))

;Animated
;----------------------------------------------------------------------------------------------------------------------
(def animated-view (partial rc/create-element-js (obj/getValueByKeys ReactNative (name :Animated) (name :View))))


(defn remove-yellow-box
  "Remove yellow box warning after 2 sec for convenience"
  []
  ((obj/getValueByKeys ReactNative "YellowBox" "ignoreWarnings") #js["Feature :formatters"
                                                                     "console"]))

(def dev? js/window.__DEV__)


(def prod? (not dev?))

