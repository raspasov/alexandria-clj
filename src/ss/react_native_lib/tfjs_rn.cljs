(ns ss.react-native-lib.tfjs-rn
 (:require
  ["@tensorflow/tfjs" :as -tfjs]
  ["@tensorflow/tfjs-react-native" :as -tfrn]
  [ss.expo.camera :as ss.expo.cam]
  [ss.react.core :as rc]
  [ss.react.state-fns :as ss.stf]
  [clojure.core.async :as a]
  [taoensso.timbre :as timbre]
  [cljs-bean.core :as b]))


(def ^js/Object tfrn -tfrn)
(def detect-gl-capabilities (.-detectGLCapabilities tfrn))
(def ^js/Object tfjs -tfjs)
(def tidy (.-tidy tfjs))

(def front-camera 2)
(def back-camera 1)
(def -camera-with-tensors (.-cameraWithTensors tfrn))
(def TensorCamera (-camera-with-tensors ss.expo.cam/Camera))
(def regular-camera-view (partial rc/create-element-js ss.expo.cam/Camera))
(def tensor-camera-view (partial rc/create-element-js TensorCamera))


(defonce *gl-capabilities (atom nil))


(defn take-picture [ref-k]
 (a/go
  (if-let [ref (ss.stf/ref ref-k)]
   (let [[ret ?error] (a/<! (.takePictureAsync (.-camera ref)))]
    (timbre/info "Took picture")
    (when ?error
     (timbre/warn ?error)
     (timbre/spy (b/->clj ?error)))
    (timbre/spy (b/->clj ret)))
   (timbre/warn "camera ref-k" ref-k "not found"))))


(defn record-async [ref-k]
 (a/go
  (if-let [^js ref (ss.stf/ref ref-k)]
   (let [camera-obj (or (.-camera ref) ref)
         [ret ?error] (a/<! (.recordAsync camera-obj))]
    (timbre/info "Recorded video ...")
    (when ?error
     (timbre/warn ?error)
     (timbre/spy (b/->clj ?error)))
    (timbre/spy (b/->clj ret)))
   (timbre/warn "camera ref-k" ref-k "not found"))))


(defn stop-recording [ref-k]
 (if-let [^js ref (ss.stf/ref ref-k)]
  (let [camera-obj (or (.-camera ref) ref)
        ret (.stopRecording camera-obj)]
   (timbre/info "Stopped recording")
   (timbre/spy (b/->clj ret)))
  (timbre/warn "camera ref-k" ref-k "not found")))
