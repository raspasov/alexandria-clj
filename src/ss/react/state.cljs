(ns ss.react.state
  (:require [taoensso.timbre :as timbre]
            [datascript.core :as d]))


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


(defn new-datascript-conn
  ([schema]
   (let [*datascript-conn (d/create-conn schema)]
     (reset! *-conn *datascript-conn)))
  ([datoms schema]
   (let [*datascript-conn (d/conn-from-datoms datoms schema)]
     (reset! *-conn *datascript-conn))))


;Mutable state atom and fns
;----------------------------------------------------------------------------------------------------------------------
(defonce *mutable-state (atom {:refs {}}))

