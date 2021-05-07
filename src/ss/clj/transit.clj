(ns ss.clj.transit
  (:require [cognitect.transit :as transit]
            [taoensso.timbre :as timbre])
  (:import (java.io ByteArrayInputStream ByteArrayOutputStream)))


(defn ^String data-to-transit [data]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (try
      (transit/write writer data)
      (catch Exception e (timbre/warn e "faulty data::" data)))
    (.toString out)))


(defn ^Object transit-to-data [transit-data]
  (when-not (nil? transit-data)
    (let [in (ByteArrayInputStream. (.getBytes transit-data))
          reader (transit/reader in :json)]
      (transit/read reader))))
