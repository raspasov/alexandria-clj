(ns ss.clj.aleph-client
 (:require [byte-streams :as bs]
           [clojure.core.async :refer [chan close! go >! <! <!! >!! go-loop put! thread alts! alts!! timeout]]
           [manifold.deferred :as d]
           [aleph.http :as http]
           [cheshire.core :as cheshire]
           [taoensso.timbre :as timbre]))

(def post-clj-http
 ;compose three functions to achieve the same response as clj-http's
 (comp #(update % :body bs/to-string) deref http/post))

(def get-clj-http
 ;compose three functions to achieve the same response as clj-http's
 (comp #(update % :body bs/to-string) deref http/get))

(def resp-aleph-xf (map #(update % :body bs/to-string)))

(defn cheshire-parse-string-safe
 "Returns clojure data if successful. If not, warns and returns s back"
 [s]
 (try
  (cheshire/parse-string s true)
  (catch Exception e (do (timbre/warn e) [s e]))))

(def parse-json-body-xf (map #(update % :body cheshire-parse-string-safe)))

(defn core-async-channel
 [return-ch & aleph-client-args]
 (d/on-realized
  (apply http/post aleph-client-args)
  (fn [x]
   (println "success!" x)
   (put! return-ch x))
  (fn [x]
   (println "error!" x)
   (put! return-ch x)))
 return-ch)

(def get-as-stream
 (comp #(update % :body identity) deref http/get))
