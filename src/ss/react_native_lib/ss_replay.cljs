(ns ss.react-native-lib.ss-replay
 (:require
  [cljs-bean.core :as b]
  [clojure.core.async :as a]
  [ss.react-native.nat :as nat]
  [ss.react.core :as rc]
  [taoensso.timbre :as timbre]))


(def ^js SSReplayManager (.-SSReplayManager nat/NativeModules))


(defn start-recording []
 (.startRecording SSReplayManager))

(defn stop-recording [an-uuid]
 (.stopRecording SSReplayManager an-uuid))




