(ns ss.react-native.virtualized-list
  (:require [ss.react.core :as rc]
            [ss.react-native.core :as r]
            [ss.react-native.dimensions :as dm]
            [ss.react.state-fns :as state-fns]
            [ss.cljs.gg]
            [cljs-bean.core :as b]
            [taoensso.timbre :as timbre]))


(defn immutable-list-component [props]
  (let [{:keys [data ref-key horizontal windowSize initialNumToRender scrollEnabled pagingEnabled onMomentumScrollEnd onMomentumScrollBegin]
         :or   {horizontal          false windowSize 1 initialNumToRender 1 scrollEnabled true pagingEnabled true
                onMomentumScrollEnd (fn [e]) onMomentumScrollBegin (fn [e])}
         :as   props} (rc/props props)
        props-no-data (dissoc props :data)]
   (r/virtualized-list
      (-> {:getItem               (fn [data idx]
                                    ;return tuple [item-data idx] - idx needed for keyExtractor
                                    ;item-data is Clojure data (usually a map)
                                    (let [item-data (nth data idx nil)]
                                      [item-data idx]))
           :getItemCount          (fn [data] (count data))
           ;needs to return a key as string
           :keyExtractor          (fn [[item-data idx]] (str idx))
           :ref                   (state-fns/save-ref ref-key)
           :initialNumToRender    initialNumToRender
           :scrollEnabled         scrollEnabled
           :pagingEnabled         pagingEnabled
           :windowSize            windowSize
           :onMomentumScrollBegin onMomentumScrollBegin
           :onMomentumScrollEnd   onMomentumScrollEnd
           :horizontal            horizontal
           :renderItem            (fn [^js/Object js-object]
                                    (let [[item-data _] (.-item js-object)
                                          idx         (.-index js-object)
                                          render-item (:render-item props)]
                                      (render-item item-data idx)))
           :scrollEventThrottle   1}
          ;merge with props (without data)
          (merge props-no-data)
          ;convert to JS
          (b/->js)
          ;add back immutable data to the JS object
          (ss.cljs.gg/assoc-obj! "data" data)))))
(def immutable-list (rc/e immutable-list-component))


(defn get-scroll-idx-via-x
  ([^js/React.SyntheticEvent e]
   (get-scroll-idx-via-x e (dm/ww)))
  ([^js/React.SyntheticEvent e a-width]
   (js/Math.round (/ (.. e -nativeEvent -contentOffset -x) a-width))))

(defn get-scroll-idx-via-y
  ([^js/React.SyntheticEvent e]
   (get-scroll-idx-via-y e (dm/hh)))
  ([^js/React.SyntheticEvent e a-height]
   (js/Math.round (/ (.. e -nativeEvent -contentOffset -y) a-height))))


(defn scroll-to-index
  "IMPORTANT :getItemLayout must specified for this to work"
 ([vl idx]
  (scroll-to-index vl idx true))
 ([^js vl idx animated?]
  (try
   (.scrollToIndex vl (b/->js {:index idx :animated animated?}))
   (catch js/Error e (do)))
  (try
   (.scrollToIndex (.. vl -_component) (b/->js {:index idx :animated true}))
   (catch js/Error e (do)))))

(defn scroll-to-offset
  ([^js/Object vl offset]
   (scroll-to-offset vl offset true))
  ([^js/Object vl offset animated?]
   (try
     (.scrollToOffset vl (b/->js {:offset offset :animated animated?}))
     (catch js/Error e (do)))
   (try
     (.scrollToOffset (.. vl -_component) (b/->js {:offset offset :animated animated?}))
     (catch js/Error e (do)))))


(comment
  (immutable-list
    {:data        data
     :ref-key     :some-ref-key
     :render-item (fn [item idx] (a-view {:item item}))}))
