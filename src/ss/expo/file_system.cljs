(ns ss.expo.file-system
 (:require [expo-file-system :as -expo-file-system]
           [clojure.core.async :as a]
           [taoensso.timbre :as timbre]
           [cljs-bean.core :as b]))


(def ^js/Object efs -expo-file-system)

(def icloud-dir-test "file:///private/var/mobile/Library/Mobile%20Documents/iCloud~org~name~RocketBellRNCLJS/Documents/")

(defn document-directory []
 (.-documentDirectory efs))


(defn get-info-async
 [file-uri]
 (.getInfoAsync efs file-uri))


(defn read-directory-async
 [file-uri]
 (.readDirectoryAsync efs file-uri))


(defn write-as-string-async
 ([file-uri contents]
  (write-as-string-async file-uri contents nil))
 ([file-uri contents options]
  (.writeAsStringAsync efs file-uri contents)))


(defn read-as-string-async [file-uri]
 (a/go
  (let [[?result ?error] (a/<! (.readAsStringAsync efs file-uri))]
   ?result)))

(defn make-directory-async
 ([file-uri]
  (.makeDirectoryAsync efs file-uri (b/->js {:intermediates true}))))


(defn delete-async
 [file-name]
 (a/go
  (let [[ret? err?] (a/<! (.deleteAsync efs
                           (str (document-directory) file-name)))]
   (timbre/spy (b/->clj ret?)))))


(comment
 (a/go
  (let [[ret? err?] (a/<! (get-info-async (str (document-directory) "292b67e7-61d5-4f2f-95bf-a29ab3dfa954-enhanced.mov")))]
   (timbre/spy (b/->clj ret?)))))

