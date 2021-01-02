(ns ax.react.repl
  (:require [ax.react.core :as r]))

(defn debug-remotely [a-bool]
  (.debugRemotely
    (.. r/ReactNative -NativeModules -DevMenu)
    a-bool))

(defn reload []
  (.reload
    (.. r/ReactNative -NativeModules -DevMenu)))
