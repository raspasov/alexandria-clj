(ns ss.cljs.transit
  (:require [cognitect.transit :as transit]
            [taoensso.timbre :as timbre]))



;Transit reader and writers
(def reader (transit/reader :json))
(def writer (transit/writer :json))


(defn >>transit [clj-data]
  (try
    (transit/write writer clj-data)
    (catch js/Error e (do (timbre/warn ">>transit failed!") (timbre/info e) e))))


(defn <<transit [transit-data]
  (try
    (transit/read reader transit-data)
    (catch js/Error e (do (timbre/warn "<!!transit failed!") (timbre/info e) nil))))

