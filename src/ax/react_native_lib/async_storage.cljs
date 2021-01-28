(ns ax.react-native-lib.async-storage
  (:require ["@react-native-async-storage/async-storage" :as -rnas]
            [clojure.core.async :as a]
            [taoensso.timbre :as timbre]))

(def ^js/Object rnas -rnas)
(def rnas-default (.-default rnas))


(def setItem (.-setItem rnas-default))
(def getItem (.-getItem rnas-default))
(def removeItem (.-removeItem rnas-default))

(defn set-item [k data]
  (a/go
    (let [[ret error?] (a/<! (setItem (name k) (str data)))]
      (if (nil? error?)
        (timbre/info :set-item :ok k data )
        (timbre/spy error?)))))


(defn get-item [k]
  (a/go
    (let [[ret error?] (a/<! (getItem (name k)))]
      (if (nil? error?)
        (timbre/spy (cljs.reader/read-string ret))
        (timbre/info error?)
        error?))))


(defn remove-item [k]
  (a/go
    (let [[ret error?] (a/<! (removeItem k))]
      (if (nil? error?)
        (timbre/info :remove-item :ok k)
        (timbre/spy error?)))))



