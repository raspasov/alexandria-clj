(ns ax.react.root
  (:require [ax.react.state :as state]
            [ax.react.core :as rc]))



;Basic
;---------------------------------------------------------------------------------
(defn basic-root-component
  "Usage:

    (basic-root-view {:app-view app-view})

  "
  [props]
  (let [[_ root-refresh-hook] (rc/use-state (random-uuid))
        _ (reset! state/*root-refresh-hook root-refresh-hook)
        {:keys [app-view]} (rc/props-fnc props)]
    (app-view @state/*app-state)))
(def basic-root-view (rc/e basic-root-component))


;Advanced
;---------------------------------------------------------------------------------
(defn advanced-root-component
  "Usage:

   (advanced-root-view
    {:app-view         app-view
     :*datascript-conn *datascript-conn
     :*app-state       *app-state
     :app-state-fn     app-state-fn})

   "
  [props]
  (let [[_ root-refresh-hook] (rc/use-state (random-uuid))
        _ (reset! state/*root-refresh-hook root-refresh-hook)
        {:keys [app-view *app-state app-state-fn]} (rc/props-fnc props)]
    (app-view (app-state-fn @*app-state))))
(def advanced-root-view (rc/e advanced-root-component))


