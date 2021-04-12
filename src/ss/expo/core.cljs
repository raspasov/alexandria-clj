(ns ss.expo.core
  (:require [expo :as -expo]))


(def ^js/Object expo -expo)


(defn register-root-component [f]
  (.registerRootComponent expo f))
