(ns ax.core-async
  (:require [clojure.core.async :as a]
            [clojure.core.async.impl.channels]
            [taoensso.timbre :as timbre])
  #?(:clj
     (:import (clojure.core.async.impl.channels ManyToManyChannel)))
  #?(:cljs
     (:require-macros ax.core-async)))




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


(defonce *uuid->stop-ch (atom {}))

(defn stop-all-gos []
  (run!
    (fn [[uuid stop-ch]]
      (a/put! stop-ch :stop)
      (swap! *uuid->stop-ch dissoc uuid))
    @*uuid->stop-ch))

(defn start-go
  "start-go-fn - fn that start a go, takes new-uuid and stop-ch; needs to take care of stopping the go via alts!"
  [start-go-fn]
  (let [stop-ch  (a/chan 1)
        new-uuid (random-uuid)]
    (swap! *uuid->stop-ch (fn [m] (assoc m new-uuid stop-ch)))
    (start-go-fn new-uuid stop-ch)))



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




(defn go-stop-info [msg]
  (timbre/info msg))



(defmacro go-stop-loop [bindings & body]
  (do
    #_(println "go-stop-loop...")
    #_(clojure.pprint/pprint &form)
    `(start-go
       (fn [new-uuid# stop-ch#]
         (a/go
           (loop ~bindings
             (let [[ret# ret-ch#] (a/alts! [stop-ch#] :default :continue)]
               (if (and (= ret# :stop) (= ret-ch# stop-ch#))
                 (do
                   (go-stop-info ["Stopping ch" new-uuid#]))
                 ;else, continue
                 (do
                   ~@body)))))))))





