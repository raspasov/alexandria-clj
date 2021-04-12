(ns ss.react.state-fns
  (:require [ss.react.state :as state]
            [taoensso.timbre :as timbre]))

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


(defn ^js/Object get-global-ref [k]
  (get-in @state/*mutable-state [:refs k]))


(defn touch []
  (swap! state/*app-state (fn [m] (assoc m :touch (random-uuid))))
  true)


(defn swap-hook! [k f]
  (let [path    [:hooks k]
        ?hook-f (get-mutable [:hooks k])]
    (if (fn? ?hook-f)
      (?hook-f f)
      (timbre/warn "No hook found at path" path))))


(defn hook-state []
  (into
    {}
    (map (fn [[k f]] [k (f identity)]))
    (get-mutable [:hooks])))
