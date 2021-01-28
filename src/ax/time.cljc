(ns ax.time)


(defn timestamp []
  #?(:cljs (js/Date.now)
     :clj  (System/currentTimeMillis)))
