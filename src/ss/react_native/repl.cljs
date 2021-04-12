(ns ss.react-native.repl
  (:require [ss.react-native.core :as rn]))

(defn debug-remotely [a-bool]
  (.debugRemotely
    (.. rn/ReactNative -NativeModules -DevMenu)
    a-bool))

(defn reload []
  (.reload
    (.. rn/ReactNative -NativeModules -DevMenu)))
