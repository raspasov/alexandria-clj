(ns ss.core-async
  (:require [clojure.core.async :as a]
            [clojure.core.async.impl.channels]
            [taoensso.timbre :as timbre])
  #?(:clj
     (:import (clojure.core.async.impl.channels ManyToManyChannel)))
  #?(:cljs
     (:require-macros ss.core-async)))


(defn channel? [x]
  #?(:cljs
     (instance? clojure.core.async.impl.channels/ManyToManyChannel x))
  #?(:clj
     (instance? ManyToManyChannel x)))


#?(:clj
   (defn random-uuid []
     (java.util.UUID/randomUUID)))

(defonce stop-chs (atom {}))


(defn stop-all-chs []
  (run!
    (fn [[uuid stop-ch]]
      (a/put! stop-ch :stop)
      (swap! stop-chs dissoc uuid))
    @stop-chs))


(defn start-go-loop [ch]
  (let [stop-ch (a/chan 1)
        uuid    (str (random-uuid))]
    (swap! stop-chs assoc uuid stop-ch)
    (a/go (loop []
            (let [[ret ret-ch] (a/alts! [stop-ch ch] :priority true)]
              (if (and (= ret :stop) (= ret-ch stop-ch))
                (do
                  (timbre/spy ["Stopping ch" uuid]))
                (do
                  (timbre/info "Channel:" uuid "|" "Got:" ret)
                  (recur))))))
    ch))


(defn create-countdown-trigger
  "Creates a countdown trigger that runs in timeout-ms unless a value is put onto the trigger-control channel,
   which resets the timeout"
  [timeout-ms f]
  (let [trigger-control (a/chan (a/dropping-buffer 1))]
    (a/go
      (loop [timeout-ch (a/timeout timeout-ms)]
        (let [[value _] (a/alts! [timeout-ch trigger-control])]
          (condp = value
            ;time to trigger the trigger
            nil (do
                  ;(timbre/info "nil...")
                  ;(timbre/spy value)
                  (a/close! trigger-control) (f))
            ;stop the countdown trigger
            :stop (do
                    ;(timbre/info "stop...")
                    ;(timbre/spy value)
                    nil)
            ;continue the trigger
            :continue
            (do
              ;(timbre/info "continue...")
              ;(timbre/spy value)
              (recur (a/timeout timeout-ms)))
            ;else, stop
            (do
              ;(timbre/info "else...")
              ;(timbre/spy value)
              nil)))))
    trigger-control))










