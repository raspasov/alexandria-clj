(ns ss.react-native-lib.haptic-feedback
 (:require [react-native-haptic-feedback :as haptic]))


(def RNReactNativeHapticFeedback ^js/Object (.-default ^js/Object haptic))


(defn trigger []
 ((.-trigger RNReactNativeHapticFeedback) "impactMedium"))

