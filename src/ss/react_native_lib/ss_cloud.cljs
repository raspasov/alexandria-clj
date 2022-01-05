(ns ss.react-native-lib.ss-cloud
 (:require
  [cljs-bean.core :as b]
  [clojure.core.async :as a]
  [ss.react-native.nat :as nat]
  [ss.react.core :as rc]
  [ss.expo.file-system :as ss.expo.fs]
  [taoensso.timbre :as timbre]))


(def ^js SSCloudManager (.-SSCloudManager nat/NativeModules))


(defn documents-path []
 (.documentsPath SSCloudManager ""))


(defn save [relative-path contents]
 (a/go
  (let [[?result ?error] (a/<! (.save SSCloudManager relative-path contents))]
   (timbre/spy [?result ?error]))))


(defn read [relative-path]
 (a/go
  (let [[icloud-path _] (a/<! (documents-path))
        _         (timbre/spy icloud-path)
        full-path (str icloud-path relative-path)
        _         (timbre/spy full-path)
        read-v    (a/<! (ss.expo.file-system/read-as-string-async full-path))]
   read-v)))




(comment
 (a/go
  (let [[icloud-path _] (a/<! (documents-path))
        ;[access? ?error] (a/<! (icloud-start-access (str icloud-path "p1")))
        ]
   (timbre/spy icloud-path))))
