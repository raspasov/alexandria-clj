(ns ss.expo.screen-orientation
 (:require [expo-screen-orientation :as -orientation]
           [clojure.core.async :as a]
           [taoensso.timbre :as timbre]))

(def ^js/Object orientation -orientation)

(def lock-PORTRAIT (.. orientation -OrientationLock -PORTRAIT))
(def lock-PORTRAIT-UP (.. orientation -OrientationLock -PORTRAIT_UP))
(def lock-PORTRAIT-DOWN (.. orientation -OrientationLock -PORTRAIT_DOWN))

(def lock-LANDSCAPE-LEFT (.. orientation -OrientationLock -LANDSCAPE_LEFT))


(defn lock-async [lock]
 (a/go
  (timbre/spy
   (a/<! (.lockAsync orientation lock)))))


(defn unlock-async []
 (a/go
  (timbre/spy
   (a/<! (.unlockAsync orientation)))))


(defn supports-orientation-lock [lock]
 (a/go
  (timbre/spy
   (a/<! (.supportsOrientationLockAsync orientation lock)))))

