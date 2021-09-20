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


(defn create-evidence-trigger
 "Creates an evidence trigger that runs if and only if the 'pred' function is satisfied within 'timeout-ms'.
  'pred' takes init-state/evidence-state."
 [pred init-state timeout-ms f-success]
 (let [control-ch (a/chan (a/dropping-buffer 1))]
  (a/go
   (loop [timeout-ch     (a/timeout timeout-ms)
          evidence-state init-state]
    (if (pred evidence-state)
     (do
      ;enough evidence collected, success!
      (timbre/spy "success...")
      (f-success)
      (recur (a/timeout timeout-ms) init-state))

     (let [[[?control-k ?evidence-f :as value] _] (a/alts! [timeout-ch control-ch])]
      (cond
       (nil? value)
       ;not enough evidence within timeout-ms, end trigger
       (do
        ;(timbre/info "Not enough evidence within timeout-ms, reset state")
        ;(timbre/spy "nil... reset state")
        ;(timbre/spy value)
        (recur (a/timeout timeout-ms) init-state))
       ;stop the trigger
       (= ?control-k :stop)
       (do
        (timbre/info "stop...")
        (timbre/spy value)
        (a/close! control-ch)
        nil)
       ;got evidence, update the evidence state and recur
       (and (= ?control-k :evidence) (fn? ?evidence-f))
       (do
        (timbre/info "evidence...")
        (timbre/spy value)
        (recur timeout-ch (?evidence-f evidence-state)))
       ;else, stop in all other cases
       :else
       (do
        (timbre/info "else...")
        (timbre/spy value)
        (a/close! control-ch)
        nil))))))
  ;return
  control-ch))

(comment
 (def evidence-ch
  (create-evidence-trigger
   (fn [x] (<= 2 x))
   0
   10000
   (fn [] (timbre/info "Evidence Success")))))


(defmacro go-try
 "Asynchronously executes the body in a go block. Returns a channel which
 will receive the result of the body when completed or an exception if one
 is thrown."
 [& body]

 (if (:ns &env)
  `(a/go (try ~@body (catch js/Error e# (do (timbre/warn (.-message e#)) (timbre/error e#)))))
  `(a/go (try ~@body (catch Throwable e# (timbre/error e#))))))





