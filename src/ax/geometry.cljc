(ns ax.geometry)


(defn switch-plane
  "Translate [x y] from source to dest coordinate system"
  [[x y] [source-x source-y] [dest-x dest-y]]
  (let [x-mult (/ dest-x source-x)
        y-mult (/ dest-y source-y)]
    [(* x x-mult) (* y y-mult)]))
