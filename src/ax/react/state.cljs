(ns ax.react.state)


(defonce *root-component-instance (atom nil))

(def initial-app-state {})


(defonce *app-state (atom initial-app-state))


(add-watch *app-state :watch-1 (fn [_ _ _ _]
                                 (when-let [^js/Object root-instance @*root-component-instance]
                                   (.forceUpdate root-instance))))

(defn set-initial-app-state!
  "Set this one when the application launches"
  [m]
  (reset! *app-state m))

;Mutable state atom and fns
;----------------------------------------------------------------------------------------------------------------------
(defonce *mutable-state (atom {:refs {}}))

