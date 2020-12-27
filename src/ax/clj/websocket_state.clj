(ns ax.clj.websocket-state
  (:require [clojure.core.async :refer [<!! >!!]]
            [taoensso.timbre :as timbre])
  (:import (clojure.core.async.impl.channels ManyToManyChannel)))

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(def byte-array-class (class (byte-array 0)))

;WebSockets state
;map that holds { socket-key uuid } pairs
(def socket-uuid (ref {}))

(defn get-socket-uuid
  "Get the user-id for a socket-key"
  [socket-key]
  (get @socket-uuid socket-key))

;map that holds a map like this: { uuid {socket-key-1 {:input-ch (chan) :output-ch (chan)}, socket-key-2 {:input-ch (chan) :output-ch (chan)}} }
(def uuid-sockets (ref {}))

(defn get-sockets
  "Get all sockets for that user on this server"
  [uuid]
  (get @uuid-sockets uuid))


(defn uuid-socket-update-timestamp
  "Updates the timestamp on the specified uuid and socket-key pair for heartbeat purposes"
  [uuid socket-key]
  (dosync (alter uuid-sockets assoc-in [uuid socket-key :timestamp] (System/currentTimeMillis))))

;socket transaction functions
(defn add-socket
  "Modifies (adds) socket-uuid and uuid-sockets maps with a Clojure STM transaction"
  [uuid socket-key ws-chans]
  (dosync
    (alter socket-uuid assoc socket-key uuid)
    (alter uuid-sockets assoc-in [uuid socket-key] (assoc ws-chans :timestamp (System/currentTimeMillis)))))

(defn remove-socket
  "Modifies (removes) socket-uuid and uuid-sockets maps with a Clojure STM transaction"
  [uuid socket-key]
  (dosync
    (alter socket-uuid dissoc socket-key)
    (alter uuid-sockets dissoc-in [uuid socket-key])))

(defn send-to-socket!
  "Sends byte-array to a websocket socket"
  [uuid socket-key b-a]
  (let [output-ch (get-in @uuid-sockets [uuid socket-key :output-ch])]
    (if (instance? ManyToManyChannel output-ch)
      (>!! output-ch b-a)
      false)))


(defn send-to-sockets! [uuid x]
  "Sends byte-array to all websockets for that user on this server (one or more)"
  (if (or (instance? String x)
          (instance? byte-array-class x))
    (doseq [{:keys [output-ch]} (vals (get @uuid-sockets uuid))]
      (>!! output-ch x))
    (timbre/warn "NOT SENT!")))





