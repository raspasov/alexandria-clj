(ns ss.core)

(defn deep-merge-with
 "Like merge-with, but merges maps recursively, applying the given fn
 only when there's a non-map at a particular level.
 (deep-merge-with + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
                    {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
 -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}"
 [f & maps]
 (apply
  (fn m [& maps]
   (if (every? map? maps)
    (apply merge-with m maps)
    (apply f maps)))
  maps))


(def conjv (fnil conj []))


#?(:clj
   (defn random-uuid []
    (java.util.UUID/randomUUID)))


(defn clear-nils
 "Remove the keys from m for which the value is nil"
 [m]
 (apply
  dissoc m
  (for [[k v] m :when (nil? v)] k)))


(defn arity-0
 "Adds a zero arity to a function that expects at least one argument."
 ([f default]
  (fn
   ([] (f default))
   ([& args] (apply f args)))))


(defn fpred
 "Like fnil, but with a custom predicate"
 ([f pred x]
  (fn
   ([a] (f (if (pred a) x a)))
   ([a b] (f (if (pred a) x a) b))
   ([a b c] (f (if (pred a) x a) b c))
   ([a b c & ds] (apply f (if (pred a) x a) b c ds))))
 ([f pred x y]
  (fn
   ([a b] (f (if (pred a) x a) (if (pred b) y b)))
   ([a b c] (f (if (pred a) x a) (if (pred b) y b) c))
   ([a b c & ds] (apply f (if (pred a) x a) (if (pred b) y b) c ds))))
 ([f pred x y z]
  (fn
   ([a b] (f (if (pred a) x a) (if (pred b) y b)))
   ([a b c] (f (if (pred a) x a) (if (pred b) y b) (if (pred c) z c)))
   ([a b c & ds] (apply f (if (pred a) x a) (if (pred b) y b) (if (pred c) z c) ds)))))


(defn name? [x]
 #?(:clj  (if (or (instance? clojure.lang.Named x)
               (string? x))
           true
           false)
    :cljs (if (or (implements? INamed x)
               (string? x))
           true
           false)))


(defn name2
 "Safer (name x)"
 [x]
 ((fpred name #(not (name? %)) "") x))


(defn swap-many!
 "Takes one or more functions and applies them to atom in order, left to right."
 ([a & fns]
  (swap! a (apply comp (filter fn? (rseq fns))))))


(def mapv-indexed (comp vec map-indexed))

(def removev (comp vec remove))


(defn positions
 [pred coll]
 (keep-indexed
  (fn [idx x] (when (pred x) idx))
  coll))


(defn print-separator [msg]
 (let [filler-cnt      (- 78 (count msg))
       half-filler-cnt (/ (int filler-cnt) 2)
       half-filler     (apply str (repeat half-filler-cnt ";"))]

  (println
   half-filler
   msg
   half-filler)))


(comment

 (let [a (atom {})]
  (swap-many! a
   #(assoc % :a 1)
   #(dissoc % :a))
  ;=> {}

  (swap-many! a
   #(dissoc % :a)
   #(assoc % :a 1))
  ;=> {:a 1}

  (swap-many! a
   #(dissoc % :a)
   #(assoc % :a 1)
   ;ability to skip fn
   (when false
    #(assoc % :cant-see-me 42)))

  ))
