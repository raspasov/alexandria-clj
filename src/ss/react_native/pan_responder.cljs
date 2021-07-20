(ns ss.react-native.pan-responder
 (:require
  [ss.react.core :as rc]
  [ss.react-native.core :as r]
  [ss.cljs.gg]
  [ss.react-native.animated :as anim]
  [ss.react-native.core :as rn]
  [taoensso.timbre :as timbre]
  [cljs-bean.core :as b]))


(rc/defnrc view- [props]
 ;DO NOT USE, just an example
 (let [pan                   (rc/use-ref (new (.-Animated.ValueXY rn/ReactNative)))
       pan-current           (rc/current pan)
       pan-responder         (rc/use-ref
                              (r/pan-responder-create
                               {:onMoveShouldSetPanResponder (fn [] true)
                                :onPanResponderGrant         (fn []
                                                              (.setOffset pan-current
                                                               (clj->js
                                                                {:x (.. pan-current -x -_value)
                                                                 :y (.. pan-current -y -_value)})))
                                :onPanResponderMove          (fn [evt gesture-state]
                                                              (let [dx (.-dx gesture-state)
                                                                    dy (.-dy gesture-state)]
                                                               (.setValue (.-x pan-current) dx)
                                                               (.setValue (.-y pan-current) dy)))
                                :onPanResponderRelease       (fn []
                                                              (.flattenOffset pan-current))}))
       pan-responder-current (rc/current pan-responder)
       pan-handlers          (.-panHandlers pan-responder-current)]


  (r/animated-view
   (merge
    (b/->clj pan-handlers)
    {:style
     {:transform [{:translateX (.-x pan-current)} {:translateY (.-y pan-current)}]
      :width     100 :height 100 :backgroundColor "white"}}))))
(def view (rc/e view-))

