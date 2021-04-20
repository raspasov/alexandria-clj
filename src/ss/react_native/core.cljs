(ns ss.react-native.core
 (:require
  [ss.react.core :as rc]
  [cljs-bean.core :as b]
  [react-native]
  [taoensso.timbre :as timbre]))


(def ^js/Object ReactNative react-native)

(def createAnimatedComponent (.. ReactNative -Animated -createAnimatedComponent))

(def ^js/Object AppRegistry (.-AppRegistry ReactNative))

(def runAfterInteractions (.. ReactNative -InteractionManager -runAfterInteractions))


(defn measure
 "Callback receives args like [x y width height pageX pageY]"
 [^js/Object ref callback]
 (.measure ref callback))


(defn- -get-alert-fn [] (.. ReactNative -Alert -alert))


(defn alert
 ([title]
  ((-get-alert-fn) title))
 ([title message]
  ((-get-alert-fn) title message #js[]))
 ([title message button-specs]
  ((-get-alert-fn) title message (b/->js button-specs))))


(def view (partial rc/create-element-js (.-View ReactNative)))
(def view|a (partial rc/create-element-js (createAnimatedComponent (.-View ReactNative))))


(def safe-area-view (partial rc/create-element-js (.-SafeAreaView ReactNative)))


(def button (partial rc/create-element-js (.-Button ReactNative)))


(def text (partial rc/create-element-js (.-Text ReactNative)))
(def text|a (partial rc/create-element-js (createAnimatedComponent (.-Text ReactNative))))


(def image (partial rc/create-element-js (.-Image ReactNative)))


(def touchable-opacity (partial rc/create-element-js (.-TouchableOpacity ReactNative)))
(def touchable-opacity|a (partial rc/create-element-js (createAnimatedComponent (.-TouchableOpacity ReactNative))))

(def pressable (partial rc/create-element-js (.-Pressable ReactNative)))


(def modal (partial rc/create-element-js (.-Modal ReactNative)))


(def activity-indicator (partial rc/create-element-js (.-ActivityIndicator ReactNative)))


(def ^js/Object status-bar (.-StatusBar ReactNative))


(def ^js/Object pixel-ratio (.-PixelRatio ReactNative))


(def ^js/Object virtualized-list (partial rc/create-element-js (createAnimatedComponent (.-VirtualizedList ReactNative))))


(def scroll-view (partial rc/create-element-js (.-ScrollView ReactNative)))


(def keyboard-avoiding-view (partial rc/create-element-js (.-KeyboardAvoidingView ReactNative)))


(def text-input (partial rc/create-element-js (.-TextInput ReactNative)))


(def platform (.. ReactNative -Platform -OS))


(def ^js/Object Keyboard (.-Keyboard ReactNative))


(defn keyboard-dismiss []
 (.dismiss Keyboard))


(defn keyboard-add-listener
 "Add a Keyboard listener."
 [^js/String event-name callback]
 (.addListener Keyboard event-name callback))


(defn keyboard-remove-listener
 "Removes a SPECIFIC listener for a Keyboard event-name.
  IMPORTANT: Original callback fn must be provided."
 [^js/String event-name callback]
 (.removeListener Keyboard event-name callback))


(defn keyboard-remove-all-listeners
 "Remove ALL listeners for a Keyboard event-name."
 [^js/String event-name]
 (.removeAllListeners Keyboard event-name))


(defn use-keyboard-status
 "Helper hook for Keyboard status.
  Returns nil until keyboard is shown for the first time.
  Return values #{:kb/shown :kb/hidden nil}"
 []
 (let [[kb-status set-kb-status :as ret] (rc/use-state nil)
       kb-show     (fn [] (set-kb-status :kb/shown))
       kb-hide     (fn [] (set-kb-status :kb/hidden))
       _           (rc/use-effect
                    (fn []
                     (timbre/info "use-keyboard init")
                     (keyboard-add-listener "keyboardWillShow" kb-show)
                     (keyboard-add-listener "keyboardDidShow" kb-show)
                     (keyboard-add-listener "keyboardDidHide" kb-hide)
                     (fn cleanup []
                      (timbre/info "use-keyboard cleanup")
                      (keyboard-remove-listener "keyboardWillShow" kb-show)
                      (keyboard-remove-listener "keyboardDidShow" kb-show)
                      (keyboard-remove-listener "keyboardDidHide" kb-hide)))
                    #js[])]
  ret))


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
  (f)))


(def dev? js/window.__DEV__)


(def prod? (not dev?))

