(ns ss.react-native-lib.shared-element
 (:require [react-native-shared-element :as -rn-shared-element]
           [ss.react.core :as rc]))

(def ^js rn-shared-element -rn-shared-element)

(def nodeFromRef (.-nodeFromRef rn-shared-element))
(def SharedElement (.-SharedElement rn-shared-element))
(def SharedElementTransition (.-SharedElementTransition rn-shared-element))



(def shared-element (partial rc/create-element-js SharedElement))
(def shared-element-transition (partial rc/create-element-js SharedElementTransition))
