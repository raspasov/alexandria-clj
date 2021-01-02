(ns ax.core-async
  (:require [clojure.core.async :refer [chan go <! >! put! alts!]]
            [clojure.core.async.impl.channels]
            [taoensso.timbre :as timbre]))


(defn channel? [x]
  (instance? clojure.core.async.impl.channels/ManyToManyChannel x))


#?(:clj
   (defn random-uuid []
     (java.util.UUID/randomUUID)))

(defonce stop-chs (atom {}))





(defn stop-all-chs []
  (run!
    (fn [[uuid stop-ch]]
      (put! stop-ch :stop)
      (swap! stop-chs dissoc uuid))
    @stop-chs))


(defn start-go-loop [ch]
  (let [stop-ch (chan 1)
        uuid    (str (random-uuid))]
    (swap! stop-chs assoc uuid stop-ch)
    (go (loop []
          (let [[ret ret-ch] (alts! [stop-ch ch] :priority true)]
            (if (and (= ret :stop) (= ret-ch stop-ch))
              (do
                (timbre/spy ["Stopping ch" uuid]))
              (do
                (timbre/info "Channel:" uuid "|"  "Got:" ret)
                (recur))))))
    ch))

;------------------

(defonce *go->uuid (atom {}))

(defn stop-all-gos []
  (run!
    (fn [[uuid stop-ch]]
      (put! stop-ch :stop)
      (swap! *go->uuid dissoc uuid))
    @*go->uuid))

(defn start-go
  "start-go-fn - fn that start a go, takes new-uuid, stop-ch; needs to take care of stopping the go via alts!"
  [start-go-fn]
  (let [stop-ch (chan 1)
        new-uuid (random-uuid)]
    (swap! *go->uuid (fn [m] (assoc m new-uuid stop-ch)))
    (start-go-fn new-uuid stop-ch)))



