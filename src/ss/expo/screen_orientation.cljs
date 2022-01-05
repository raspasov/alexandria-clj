(ns ss.expo.screen-orientation
 (:require [expo-screen-orientation :as -orientation]
           [clojure.core.async :as a]
           [taoensso.timbre :as timbre]))

(def ^js/Object orientation -orientation)

(def lock-PORTRAIT (.. orientation -OrientationLock -PORTRAIT))
(def lock-PORTRAIT-UP (.. orientation -OrientationLock -PORTRAIT_UP))
(def lock-PORTRAIT-DOWN (.. orientation -OrientationLock -PORTRAIT_DOWN))
(def lock-LANDSCAPE-LEFT (.. orientation -OrientationLock -LANDSCAPE_LEFT))
(def lock-LANDSCAPE-RIGHT (.. orientation -OrientationLock -LANDSCAPE_RIGHT))


(defn lock-async [lock]
 (a/go
  (timbre/spy
   (a/<! (.lockAsync orientation lock)))))


(defn unlock-async []
 (a/go
  (timbre/spy
   (a/<! (.unlockAsync orientation)))))


(defn degrees->lock [deg]
 (condp = deg
  0 lock-PORTRAIT-UP
  -90 lock-LANDSCAPE-RIGHT
  90 lock-LANDSCAPE-LEFT
  180 lock-PORTRAIT-UP
  lock-PORTRAIT-UP))


(defn supports-orientation-lock [lock]
 (a/go
  (timbre/spy
   (a/<! (.supportsOrientationLockAsync orientation lock)))))


(defn get-orientation-async []
 (a/go
  (timbre/spy
   (a/<! (.getOrientationAsync orientation)))))

(defn get-orientation-lock-async []
 (a/go
  (timbre/spy
   (a/<! (.getOrientationLockAsync orientation)))))
