(ns ax.clj.transit
  (:require [clj-time.coerce :as coerce]
            [cognitect.transit :as transit])
  (:import (java.io ByteArrayInputStream ByteArrayOutputStream)
           (org.joda.time DateTime)))


(def joda-time-writer
  (transit/write-handler
    (constantly "m")
    #(-> % coerce/to-date .getTime)
    #(-> % coerce/to-date .getTime .toString)))


(defn ^String data-to-transit [data]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json {:handlers {DateTime joda-time-writer}})]
    (try
      (transit/write writer data)
      (catch Exception e (println e "faulty data::" data)))
    (.toString out)))


(defn ^Object transit-to-data [transit-data]
  (when-not (nil? transit-data)
    (let [in (ByteArrayInputStream. (.getBytes transit-data))
          reader (transit/reader in :json)]
      (transit/read reader))))
