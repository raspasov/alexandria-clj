(ns ss.react-native-lib.animatable
 (:require [react-native-animatable :as -rna]
           [ss.react.core :as rc]
           [cljs-bean.core :as b]
           [ss.react-native.dimensions :as dm]
           [ss.react-native.core :as r]))

(def ^js/Object rna -rna)


(def view (partial rc/create-element-js (.-View rna)))


(def text (partial rc/create-element-js (.-Text rna)))


(def touchable-opacity
 (partial rc/create-element-js
  ((.-createAnimatableComponent rna)
   (.-TouchableOpacity r/ReactNative))))


(def safe-area-view
 (partial rc/create-element-js
  ((.-createAnimatableComponent rna)
   (.-SafeAreaView r/ReactNative))))


(def animatable-registry
 (.initializeRegistryWithDefinitions
  rna
  (b/->js
   {:removeFromList {:useNativeDriver true
                     :from            {:height (dm/<>2 100)}
                     :to              {:height 0} :duration 200}
    :bounceBounce   {0   {:scale 1}
                     0.3 {:scale 0.95}
                     1   {:scale 1}}
    :pulsePulse     {0   {:scale   1
                          :opacity 1}
                     0.5 {:scale   1.3
                          :opacity 0.5}
                     1   {:scale   1
                          :opacity 1}}})))
