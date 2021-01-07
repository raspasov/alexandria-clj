(ns ax.react.state-fns
  (:require [ax.react.state :as state]))

(defn- ^PersistentVector build-vector
  "Builds a vector from either k or sequence of ks"
  [k-or-ks]
  (apply conj [] (if (sequential? k-or-ks) k-or-ks [k-or-ks])))
;memoize build-vector
(def build-vector-mem (memoize build-vector))


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


