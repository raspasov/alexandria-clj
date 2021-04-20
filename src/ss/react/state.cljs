(ns ss.react.state
 (:require [taoensso.timbre :as timbre]
           [datascript.core :as d]
           [clojure.core.async :as a]))


(def initial-app-state {})


(defonce *app-state (atom initial-app-state))


(defn set-initial-app-state!
 "Set this one when the application launches"
 [m]
 (reset! *app-state m))


(defonce *root-refresh-hook (atom nil))


(defn watch-refresh-hook [watch-key -atom old-state new-state]
 (if (= old-state new-state)
  (timbre/spy (= old-state new-state))
  (when-let [refresh-root-hook @*root-refresh-hook]
   (refresh-root-hook (random-uuid)))))


(add-watch *app-state :watch-1 watch-refresh-hook)


(defonce *-conn (atom nil))

(defn *conn [] @*-conn)


(defonce datascript-ready-ch (a/promise-chan))


(defn set-datascript-ready []
 (a/put! datascript-ready-ch :ready/datascript)
 (swap! *app-state assoc :ready/datascript? true))


(defn new-datascript-conn
 ([]
  ;just set to ready
  (set-datascript-ready))
 ([schema]
  (let [*datascript-conn (d/create-conn schema)
        ret              (reset! *-conn *datascript-conn)]
   (set-datascript-ready)
   ret))
 ([datoms schema]
  (let [*datascript-conn (d/conn-from-datoms datoms schema)
        ret              (reset! *-conn *datascript-conn)]
   (set-datascript-ready)
   ret)))


;Mutable state atom and fns
;----------------------------------------------------------------------------------------------------------------------
(defonce *mutable-state (atom {:refs {}}))

