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

(defn start-recording [an-uuid]
 (timbre/info "start-recording" an-uuid)

 (.startRecording SSCameraManager (str an-uuid)))

(defn stop-recording []
 (a/go
  (let [stop-rec-result (a/<! (.stopRecording SSCameraManager ""))]
   (timbre/spy stop-rec-result)
   #_(timbre/spy stop-recording-ret?)
   #_(timbre/spy err?))))

(defn start-pose-tracking []
 (.startPoseTracking SSCameraManager))

(defn stop-pose-tracking []
 (.stopPoseTracking SSCameraManager))

(defn start-hand-tracking []
 (.startHandTracking SSCameraManager))

(defn stop-hand-tracking []
 (.stopHandTracking SSCameraManager))


(defn merge-videos [video-specs]
 (timbre/spy video-specs)
 (.mergeVideos SSCameraManager (b/->js video-specs)))


(defn enhance-video [an-uuid exercise-name]
 (.enhanceVideo SSCameraManager an-uuid exercise-name))


(comment
 (merge-videos
  (into []
   (comp
    (map (fn [-uuid] {:uuid (str -uuid) :start 10 :duration 5})))

   (ar.datascript/workout-set-uuids (ar.datascript/db) 988)
   #_(mapv :exercise-set/uuid
      (mapv ar.datascript/exercise-set
       (take 2 (ar.datascript/all-exercise-sets-eids (ar.datascript/db))))))))


(rc/defnrc -camera-view [{:keys [cameraType] :as props}]
 (let [_ (rc/use-effect-once
          (fn []
           (timbre/info "ss-camera init")
           (fn cleanup []
            ;(stop-camera)
            ;(stop-camera)
            (timbre/info "ss-camera cleanup."))))]
  (timbre/info "RENDER ss-camera" cameraType)
  (ss-camera props)))
(def camera-view (rc/e -camera-view))



(comment
 (a/go
  (let [an-uuid "58af2187-5f14-4e0e-8b85-6fd46e8c0d36"]
   (a/<! (ss.expo.file-system/delete-async (str an-uuid "-enhanced.mov")))
   (enhance-video an-uuid "Muscle Up"))))

(comment
 (ss.react-native-lib.ss-camera/enhance-video "bdc3829d-1f75-4db9-b875-9be528e50af0" "Exercise!"))

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
