(ns ss.cljs.fetch
 (:require
  [cljs-bean.core :as b]
  [clojure.core.async :as a]
  [ss.cljs.transit :as ss.transit]
  [medley.core :as med]
  [taoensso.timbre :as timbre]))


(defn fetch
 "Wrapper around js/fetch"
 [url {:keys [headers body method cache]
       :or   {cache "no-cache"}
       :as   opts}]
 (js/fetch
  url
  (b/->js
   (med/assoc-some
    {}
    :headers headers
    :body body
    :method method
    :cache cache))))


(defn fetch-exists
 "Returns a channel with true or false"
 [url]
 (a/go
  (let [[^js/Object exists-ret ?error-on-resp]
        (a/<!
         (fetch url
          (-> {}
           (assoc :method "HEAD"))))
        _  (when ?error-on-resp (timbre/warn ?error-on-resp))
        ok (.-ok exists-ret)]
   ok)))


(defn fetch-transit
 "fetch, then convert response to transit"
 ([url]
  (fetch-transit url {}))
 ([url opts]
  (a/go
   (let [[ret ?error-on-resp]
         (a/<!
          (fetch url
           (-> opts
            (update :body ss.transit/>>transit)
            (assoc :method "POST")
            (assoc-in [:headers :content-type] "application/transit+json"))))
         _        (when ?error-on-resp (timbre/warn ?error-on-resp))
         [?ret-text _]
         (when (nil? ?error-on-resp)
          (a/<! (.text ret)))
         ret-data (ss.transit/<<transit ?ret-text)]
    ret-data))))


