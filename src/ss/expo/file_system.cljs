(ns ss.expo.file-system
 (:require [expo-file-system :as -expo-file-system]
           [clojure.core.async :as a]
           [taoensso.timbre :as timbre]
           [cljs-bean.core :as b]))


(def ^js/Object expo-file-system -expo-file-system)


(defn document-directory []
 (.-documentDirectory expo-file-system))


(defn get-info-async
 [file-uri]
 (.getInfoAsync expo-file-system file-uri))


(defn delete-async
 [file-name]
 (a/go
  (let [[ret? err?] (a/<! (.deleteAsync expo-file-system
                           (str (document-directory) file-name)))]
   (timbre/spy (b/->clj ret?)))))


(comment
 (a/go
  (let [[ret? err?] (a/<! (get-info-async (str (document-directory) "292b67e7-61d5-4f2f-95bf-a29ab3dfa954-enhanced.mov")))]
   (timbre/spy (b/->clj ret?)))))
