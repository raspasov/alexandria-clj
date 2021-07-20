(ns ss.react.root
 (:require [ss.react.state :as state]
           [ss.react.core :as rc]))



;Basic
;---------------------------------------------------------------------------------
(defn basic-root-
 "Usage:
   (basic-root {:app-view app-view})
 "
 [props]
 (let [[_ root-refresh-hook] (rc/use-state (random-uuid))
       _ (reset! state/*root-refresh-hook root-refresh-hook)
       {:keys [app-view]} (rc/props props)]
  (app-view @state/*app-state)))
(def basic-root (rc/e basic-root-))


;Advanced
;---------------------------------------------------------------------------------
(rc/defnrc root-
 "app-view - React view which takes the app-state as props
  *app-state - needs to be an atom so that it can be deref-ed on every update of the atom
  app-state-fn - a fn which takes the deref-ed value of the *app-state atom and return an app-state to render"
 [{:keys [app-view *app-state app-state-fn]}]
 (let [[_ root-refresh-hook] (rc/use-state (random-uuid))
       _ (reset! state/*root-refresh-hook root-refresh-hook)]
  (app-view (app-state-fn @*app-state))))
(def root (rc/e root-))


