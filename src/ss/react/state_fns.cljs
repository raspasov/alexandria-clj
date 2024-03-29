(ns ss.react.state-fns
 (:require [ss.react.state :as state]
           [taoensso.timbre :as timbre]
           [medley.core :as med]))

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


(defn dissoc-mutable! [k-or-ks]
 (swap! state/*mutable-state #(med/dissoc-in % (build-vector-mem k-or-ks))))


(defn ^js/Object get-mutable [k-or-ks]
 (get-in @state/*mutable-state (build-vector-mem k-or-ks)))


(defn ^js/cljs.core.IFn save-ref
 "Returns a function to be used for saving a ref in the global mutable state;
  Use under :ref in React.

  Optional callback should be like (fn [a-ref k])"
 ([k]
  (save-ref k nil))
 ([k callback]
  (fn [a-ref]
   (when a-ref
    (let [ret (set-mutable! [:refs k] a-ref)]
     (when (fn? callback)
      (callback a-ref k))
     ret)))))

(defn ^js/cljs.core.IFn save-ref-2
 "Added ability to use ks"
 [& ks]
 (fn [a-ref]
  (when a-ref
   (let [ret (set-mutable! (apply vector :refs ks) a-ref)]
    ret))))


(defn ^js/Object ref [k]
 (if-let [a-ref (get-in @state/*mutable-state [:refs k])]
  a-ref
  (timbre/warn "No ref found for k" k)))

(defn ^js/Object ref-2
 "Added ability to use ks"
 [& ks]
 (if-let [a-ref (get-in @state/*mutable-state (apply vector :refs ks))]
  a-ref
  (timbre/warn "No ref found for ks" ks)))

(defn ^js/Object ref-no-warn [k]
 (get-in @state/*mutable-state [:refs k]))


(defn touch []
 (swap! state/*app-state (fn [m] (assoc m :touch (random-uuid))))
 true)



(defn hook-state []
 (into
  {}
  (map (fn [[k f]] [k (f identity)]))
  (get-mutable [:hooks])))
