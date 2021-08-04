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


(comment
 (a/go
  (let [[ret? err?] (a/<! (get-info-async (str (document-directory) "qwerty.mov")))]
   (timbre/spy (b/->clj ret?)))))
