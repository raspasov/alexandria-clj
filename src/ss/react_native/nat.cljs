(ns ss.react-native.nat
 (:require [react-native$NativeModules :as -NativeModules]
           [react-native$NativeEventEmitter :as -NativeEventEmitter]
           [react-native$requireNativeComponent :as -requireNativeComponent]))

(def ^js NativeModules -NativeModules)
(def ^js NativeEventEmitter -NativeEventEmitter)
(def requireNativeComponent -requireNativeComponent)


