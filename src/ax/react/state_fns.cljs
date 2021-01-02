(ns ax.react.state-fns
  (:require [ax.react.state :as state]))

(defn- ^PersistentVector build-vector
  "Builds a vector from either k or sequence of ks"
  [k-or-ks]
  (apply conj [] (if (sequential? k-or-ks) k-or-ks [k-or-ks])))
;memoize build-vector
(def build-vector-mem (memoize build-vector))


(defn set-tab-bar-idx! [idx]
  (swap! state/*app-state #(assoc % :tab-bar/current-idx idx)))


(defn show-modal
  ([k]
   (show-modal k nil))
  ([k props]
   (swap! state/*app-state #(update % k (fn [m] (merge
                                                  (assoc m :visible? true)
                                                  props))))))


(defn set-loading [ks t-or-f]
  (swap! state/*app-state #(assoc-in % ks t-or-f)))


(defn hide-modal [k]
  (swap! state/*app-state #(update % k (fn [m] (assoc m :visible? false)))))


(defn show-registration []
  (swap! state/*app-state (fn [m] (assoc m :need-to-register? true))))


(defn touch []
  (swap! state/*app-state (fn [m] (update m :touch inc))))


(defn f-of-state
  "Apply generic f(state) function"
  [f]
  (swap! state/*app-state f))


(defn set-mutable! [k-or-ks x]
  (swap! state/*mutable-state #(assoc-in % (build-vector-mem k-or-ks) x)))


(defn update-mutable! [k-or-ks f]
  (swap! state/*mutable-state #(update-in % (build-vector-mem k-or-ks) f)))


(defn ^js/Object get-mutable [k-or-ks]
  (get-in @state/*mutable-state (build-vector-mem k-or-ks)))


(defn ^js/cljs.core.IFn save-global-ref-f
  "Returns a function to be used for saving a ref in the global mutable state;
   Use under :ref in React"
  [k]
  (fn [a-ref] (when a-ref (set-mutable! [:refs k] a-ref))))


(defn ^js/React.Component get-global-ref [k]
  (get-in @state/*mutable-state [:refs k]))


