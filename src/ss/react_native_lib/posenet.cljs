(ns ss.react-native-lib.posenet
 (:require
  ["@tensorflow-models/posenet" :as -posenet]
  ["@tensorflow/tfjs" :as -tfjs]
  [cljs-bean.core :as b]
  [ss.react-native-lib.device-info :as d.info]
  [clojure.core.async :as a]
  [ss.loop :as ss|a]
  [taoensso.timbre :as timbre]))


;Posenet
;---------------------------------------
(def ^js/Object tfjs -tfjs)
(def ^js/Object posenet -posenet)


(defn tf-memory
 "Returns TensorFlow memory usage"
 []
 ((.-memory tfjs)))


(defonce tf-ready-ch (a/promise-chan))
(defonce posenet-ready-ch (a/promise-chan))


(defn get-tf-ready []
 (a/go
  (let [ready (a/<! (.ready tfjs))
        _     (timbre/spy ready)]
   (timbre/info "TF ready!")
   (a/put! tf-ready-ch :ready/tensorflow))))


(defonce *model (atom nil))

;Model Config maps
;--------------------------------------------------
;(def w+h [200 200])
(def w+h [1080 1080])
(def input-resolution {:width (first w+h) :height (second w+h)})

(defn model-v1
 "Different Posenet model settings based on device generation"
 []
 (let [gen  (d.info/device-generation-ios)
       opts (cond
             (<= 12 gen)
             {:outputStride 16
              :multiplier   1.0
              :quantBytes   4}
             (<= 10 gen)
             {:outputStride 16
              :multiplier   0.75
              :quantBytes   2}
             :else
             {:outputStride 16
              :multiplier   0.5
              :quantBytes   2})]

  (timbre/spy opts)

  (merge
   {:architecture    "MobileNetV1"
    :inputResolution input-resolution}
   opts)))


(defn model-v2-resnet-50 []
 {:architecture    "ResNet50"
  :outputStride    16
  :quantBytes      1
  :inputResolution input-resolution})

;IMPORTANT: Select model top load here
(def chosen-model (model-v1))
;--------------------------------------------------


(defn load-posenet [model-config-map]
 (a/go
  (when (nil? @*model)
   (timbre/info "Posenet waiting for TF ready...")
   ;wait for TF ready
   (a/<! tf-ready-ch)
   (timbre/info "TF is ready, loading posenet...")
   (let [model-config (b/->js model-config-map)
         [model err] (a/<! (.load posenet model-config))]
    (if err
     (do
      (timbre/info "Posenet load error")
      (timbre/warn err)
      false)
     (do
      (timbre/info "Posenet loaded!")
      (reset! *model model)
      ;set posenet ready
      (a/>! posenet-ready-ch :ready/posenet)
      ;return
      true
      ))))))


(defn estimate-single-pose [^js/Object model tensor]
 (.estimateSinglePose model tensor (b/->js {:flipHorizontal false})))


