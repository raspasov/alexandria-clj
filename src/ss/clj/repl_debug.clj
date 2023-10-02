(ns ss.clj.repl-debug
  "A simple REPL debugger. (break) stops the code execution at any point and allows inspection of current locals.")

(defn contextual-eval [ctx expr]
  (eval
    `(let [~@(mapcat (fn [[k v]] [k `'~v]) ctx)]
       ~expr)))

(defmacro local-context []
  (let [symbols (keys &env)]
    (zipmap (map (fn [sym] `(quote ~sym))
              symbols)
      symbols)))

(defn readr [prompt exit-code]
  (let [input (clojure.main/repl-read prompt exit-code)]
    (if (= input ::tl)
      exit-code
      input)))

(defmacro break []
  `(clojure.main/repl
     :prompt #(print "repl-debug=> ")
     :read readr
     :eval (partial contextual-eval (local-context))))

;Sample usage
(comment
  (defn div [n d]
    (break)
    (int (/ n d))))
