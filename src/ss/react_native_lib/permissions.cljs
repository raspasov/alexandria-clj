(ns ss.react-native-lib.permissions
 (:require [react-native-permissions :as -RNPermissions]
           [clojure.core.async :as a]
           [taoensso.timbre :as timbre]
           [cljs-bean.core :as b]))

(def ^js/Object RNPermissions -RNPermissions)

;Permissions
(def PHOTO-LIBRARY-ADD-ONLY (.. RNPermissions -PERMISSIONS -IOS -PHOTO_LIBRARY_ADD_ONLY))

;Results
(def UNAVAILABLE (.. RNPermissions -RESULTS -UNAVAILABLE))
(def BLOCKED (.. RNPermissions -RESULTS -BLOCKED)) ;The permission is denied and not requestable anymore
(def DENIED (.. RNPermissions -RESULTS -DENIED)) ;The permission has not been requested / is denied but requestable
(def GRANTED (.. RNPermissions -RESULTS -GRANTED))
(def LIMITED (.. RNPermissions -RESULTS -LIMITED))

(defn check
 "f is a function which receives the result"
 [permission f]
 (a/go
  (let [[ret ?error] (a/<! (.check RNPermissions permission))
        result (condp = ret
                UNAVAILABLE :unavailable
                BLOCKED :blocked
                DENIED :denied
                GRANTED :granted
                LIMITED :limited)]
   (timbre/info "check" permission)
   (timbre/spy ret)
   (timbre/spy ?error)
   (f result)
   (timbre/spy result))))


(defn open-settings []
 (.openSettings RNPermissions))


(def -request (.-request RNPermissions))

(defn request [permission]
 (-request permission))

