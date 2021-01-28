(ns ax.react.state
  (:require [taoensso.timbre :as timbre]
            [datascript.core :as d]))


#_(defonce *root-component-instance (atom nil))


(def initial-app-state {})


(defonce *app-state (atom initial-app-state))


(defonce *root-refresh-hook (atom nil))

(defn watch-refresh-hook [watch-key -atom old-state new-state]
  (if (= old-state new-state)
    (timbre/spy (= old-state new-state))
    (when-let [refresh-root-hook @*root-refresh-hook]
      (refresh-root-hook (random-uuid)))))

(add-watch *app-state :watch-1 watch-refresh-hook)

(defn set-initial-app-state!
  "Set this one when the application launches"
  [m]
  (reset! *app-state m))


(defn -get-datascript-instance [schema]
  (let [*conn (d/create-conn schema)]
    (add-watch *conn :datascript-watch-1 watch-refresh-hook)
    ;return atom
    *conn))
(def get-datascript-instance (memoize -get-datascript-instance))




(defn get-initial-datascript-app-state [*conn query]
  (d/q query @*conn))

;Mutable state atom and fns
;----------------------------------------------------------------------------------------------------------------------
(defonce *mutable-state (atom {:refs {}}))

