(ns ss.react-native-lib.ss-camera
 (:require [react-native$NativeModules :as -NativeModules]
           [react-native$NativeEventEmitter :as -NativeEventEmitter]
           [react-native$requireNativeComponent :as requireNativeComponent]
           [taoensso.timbre :as timbre]
           [cljs-bean.core :as b]
           [clojure.core.async :as a]
           [ss.react.core :as rc]))




(def ^js NativeModules -NativeModules)
(def ^js NativeEventEmitter -NativeEventEmitter)

(def ^js SSCameraManager (.-SSCameraManager NativeModules))
(defonce SSCamera (requireNativeComponent "SSCamera"))
(def ss-camera (partial rc/create-element-js SSCamera))

(defn start-camera [front-or-back on-stop]
 (timbre/info "start-camera in native:::" front-or-back)
 (.startCamera SSCameraManager front-or-back on-stop))


(defn set-front-or-back [front-or-back]
 (timbre/info "set front or back" front-or-back)
 (.setFrontOrBack SSCameraManager front-or-back))


(defn stop-camera []
 (timbre/info "stop-camera in native")
 (.stopCamera SSCameraManager "stop-camera from js"))

(defn start-recording []
 (.startRecording SSCameraManager))

(defn stop-recording []
 (.stopRecording SSCameraManager))

(defn start-pose-tracking []
 (.startPoseTracking SSCameraManager))

(defn stop-pose-tracking []
 (.stopPoseTracking SSCameraManager))


(rc/defnrc -camera-view [{:keys [cameraType] :as props}]
 (let [_ (rc/use-effect-once
          (fn []
           (timbre/info "ss-camera init")
           (fn cleanup []
            ;(stop-camera)
            (timbre/info "ss-camera cleanup"))))]
  (timbre/info "RENDER ss-camera")
  (ss-camera props)))
(def camera-view (rc/e -camera-view))





;ObjC, listeners
;-----------------------------------------------------------------
;(comment
; (def listener-f
;  (fn [listener-ret]
;   (timbre/spy
;    (b/->clj listener-ret))))
;
; (def ^js AnEmitter (NativeEventEmitter. SSCameraManager))
;
; (defn add-listener [event-name f]
;  (.addListener AnEmitter event-name f))
;
; (defn remove-listener
;  "Removes a SPECIFIC listener for an event-name.
;   IMPORTANT: Original callback fn must be provided."
;  [event-name f]
;  (.removeListener AnEmitter event-name f))
;
; (defn remove-all-listeners [event-name]
;  (.removeAllListeners AnEmitter event-name)))
