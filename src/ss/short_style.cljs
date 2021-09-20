(ns ss.short-style
 (:require
  [taoensso.timbre :as timbre]))


(def shorthands
 {:f       :flex
  :fG      :flexGrow
  :D       :flexDirection
  :||      "column"
  :--      "row"
  :C       :justifyContent
  :<<      "flex-start"
  :>>      "flex-end"
  :><      "center"
  :sb      "space-between"
  :sa      "space-around"
  :se      "space-evenly"
  :I       :alignItems
  :<>      "stretch"
  :bl      "baseline"
  :S       :alignSelf
  :au      "auto"
  ;position
  :pos     :position
  :abs     "absolute"
  :rel     "relative"
  :t       :top
  :l       :left
  :b       :bottom
  :r       :right
  ;margins
  :mar     :margin
  :marL    :marginLeft
  :marR    :marginRight
  :marT    :marginTop
  :marB    :marginBottom
  :marV    :marginVertical
  :marH    :marginHorizontal
  ;padding
  :pad     :padding
  :padL    :paddingLeft
  :padR    :paddingRight
  :padT    :paddingTop
  :padB    :paddingBottom
  :padV    :paddingVertical
  :padH    :paddingHorizontal
  ;etc
  :h       :height
  :w       :width
  :baC     :backgroundColor
  :boRad   :borderRadius
  :boTLRad :borderTopLeftRadius
  :boTRRad :borderTopRightRadius
  :boBRRad :borderBottomRightRadius
  :boBLRad :borderBottomLeftRadius
  :boW     :borderWidth
  :boClr   :borderColor
  :boTW    :borderTopWidth
  :boBW    :borderBottomWidth
  :boLW    :borderLeftWidth
  :boRW    :borderRightWidth
  :clr     :color
  :zI      :zIndex
  :xf      :transform
  :minW    :minWidth
  :minH    :minHeight
  :maxW    :maxWidth
  :maxH    :maxHeight
  :op      :opacity
  :ovfl    :overflow
  :vis     "visible"
  ;font
  :fF      :fontFamily
  :fS      :fontSize
  :fW      :fontWeight
  :tA      :textAlign
  :dis     :display
  })


(defn lookup [x m]
 (if-let [ret (get m x)]
  ret
  (do
   (timbre/warn "No shorthand defined for" x)
   ;original
   x)))


(defn -style-2
 ([& args]
  (let [wrapped? (and
                  (= 1 (count args))
                  (vector? (ffirst args)))]
   (transduce
    (comp
     (if wrapped?
      (mapcat identity)
      (map identity))
     (remove #(nil? (first %)))
     (map (fn [[k v]]
           [(lookup k shorthands)
            (if (keyword? v)
             (lookup v shorthands)
             ;else use value as-is
             v)])))
    (fn
     ([accum-final]
      (persistent! accum-final))
     ([accum item]
      (conj! accum item)))
    (transient {})
    args))))


(defn -style
 ([& args]
  (let [wrapped? (and
                  (= 1 (count args))
                  (vector? (ffirst args)))]
   (transduce
    (comp
     (if wrapped?
      (mapcat identity)
      (map identity))
     (remove #(nil? (first %)))
     (map (fn [[k v]]
           [(lookup k shorthands)
            (if (keyword? v)
             (lookup v shorthands)
             ;else use value as-is
             v)])))
    conj
    {}
    args))))
(def >>> (memoize -style))

(comment
 ;Usage
 {:f #{1 2 3 #_etc}

  :D #{:|| :--}

  :C #{:<< :>> :>< :sb :sa :se}

  :I #{:<< :>> :>< :<> :bl}

  :S #{:<< :>> :>< :<> :bl}})



(defn shorten
 "Shorten a full regular style.
  regular-style must be quoted like '{:width width :height height}"
 [regular-style]
 (let [shorthands-invert (clojure.set/map-invert shorthands)
       ret               (into
                          []
                          (map (fn [[k v]]
                                [(lookup k shorthands-invert)
                                 (lookup v shorthands-invert)]))
                          regular-style)
       ret'              (apply str (rest (drop-last (str ret))))]
  (println `(x/>>> ~ret'))))


