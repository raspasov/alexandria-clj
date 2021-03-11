(ns ax.react-native.virtualized-list
  (:require [ax.react.core :as rc]
            [ax.react.macros :as macro]
            [ax.react-native.core :as r]
            [ax.react-native.dimensions :as dm]
            [ax.react.state-fns :as state-fns]
            [ax.cljs.googc :as axgoog]
            [cljs-bean.core :as b]
            [taoensso.timbre :as timbre]))


(def immutable-list-class
  (macro/create-react-class
    :render
    #(this-as this
       (let [{:keys [data ref-key horizontal windowSize initialNumToRender scrollEnabled pagingEnabled onMomentumScrollEnd]
              :or {horizontal false windowSize 1 initialNumToRender 1 scrollEnabled true pagingEnabled true
                   onMomentumScrollEnd (fn [e])} :as props} (rc/get-props-class this)
             props-no-data (dissoc props :data)]
         (r/virtualized-list
           (-> {:getItem             (fn [data idx]
                                       ;return tuple [item-data idx] - idx needed for keyExtractor
                                       ;item-data is Clojure data (usually a map)
                                       (let [item-data (nth data idx nil)]
                                         [item-data idx]))
                :getItemCount        (fn [data] (count data))
                ;needs to return a key as string
                :keyExtractor        (fn [[_ idx]] (str idx))
                :ref                 (fn [ref]
                                       (when ref-key
                                         ;(timbre/info "saving ref..." ref-key)
                                         ;(timbre/spy ref)
                                         (state-fns/set-mutable! [:refs ref-key] ref)))
                :initialNumToRender  initialNumToRender
                :scrollEnabled       scrollEnabled
                :pagingEnabled       pagingEnabled
                :windowSize          windowSize
                :onMomentumScrollEnd onMomentumScrollEnd
                :horizontal          horizontal
                :renderItem          (fn [^js/Object js-object]
                                       (let [[item-data _] (.-item js-object)
                                             idx         (.-index js-object)
                                             render-item (:render-item props)]
                                         (render-item item-data idx)))
                :scrollEventThrottle 1}
             ;merge with props (without data)
             (merge props-no-data)
             ;convert to JS
             (b/->js)
             ;add back immutable data to the JS object
             (axgoog/assoc-obj! "data" data)))))))
(def immutable-list-view (partial rc/create-element-cljs immutable-list-class))

(defn get-scroll-idx-via-x
  ([^js/React.SyntheticEvent e]
   (get-scroll-idx-via-x e (dm/width)))
  ([^js/React.SyntheticEvent e a-width]
   (js/Math.round (/ (.. e -nativeEvent -contentOffset -x) a-width))))

(defn get-scroll-idx-via-y
  ([^js/React.SyntheticEvent e]
   (get-scroll-idx-via-y e (dm/height)))
  ([^js/React.SyntheticEvent e a-height]
   (js/Math.round (/ (.. e -nativeEvent -contentOffset -y) a-height))))


(defn v-list-scroll-to-index
  "IMPORTANT :getItemLayout must specified for this to work"
  [^js/ReactNative.VirtualizedList vl idx]
  (try
    (.scrollToIndex vl (b/->js {:index idx}))
    (catch js/Error e (do)))
  (try
    (.scrollToIndex (.. vl -_component) (b/->js {:index idx}))
    (catch js/Error e (do))))

(defn v-list-scroll-to-offset
  [^js/ReactNative.VirtualizedList vl offset]
  (try
    (.scrollToOffset vl (b/->js {:offset offset}))
    (catch js/Error e (do)))
  (try
    (.scrollToOffset (.. vl -_component) (b/->js {:offset offset :animated true}))
    (catch js/Error e (do))))


(comment
  (immutable-list-view
    {:data        data
     :ref-key     :some-ref-key
     :render-item (fn [item idx] (a-view {:item item}))}))