(ns ax.react.state
  (:require [taoensso.timbre :as timbre]))


#_(defonce *root-component-instance (atom nil))


(def initial-app-state {})


(defonce *app-state (atom initial-app-state))


(defonce *root-refresh-hook (atom nil))


(add-watch *app-state :watch-1 (fn [_ _ _ _]
                                 (when-let [refresh-root-hook @*root-refresh-hook]
                                   (refresh-root-hook (random-uuid)))))

(defn set-initial-app-state!
  "Set this one when the application launches"
  [m]
  (reset! *app-state m))

;Mutable state atom and fns
;----------------------------------------------------------------------------------------------------------------------
(defonce *mutable-state (atom {:refs {}}))

