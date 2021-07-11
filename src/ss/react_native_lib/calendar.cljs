(ns ss.react-native-lib.calendar
 (:require [react-native$NativeModules :as -NativeModules]
           [react-native$NativeEventEmitter :as -NativeEventEmitter]
           [clojure.core.async :as a]
           [taoensso.timbre :as timbre]
           [cljs-bean.core :as b]))


(def ^js NativeModules -NativeModules)
(def ^js NativeEventEmitter -NativeEventEmitter)

(def ^js CalendarModule (.-CalendarModule NativeModules)) ;ObjC
(def ^js CalendarManager (.-CalendarManager NativeModules)) ;Swift

;Swift
;-----------------------------------------------------------------
(defn add-event []
 (.addEvent CalendarManager "x" "y" 123))

;ObjC
;-----------------------------------------------------------------
(defn create-calendar-event []
 (a/go
  (let [[?ret ?error] (a/<! (.createCalendarEvent CalendarModule "testName" "testLocation"))]
   (timbre/spy ?ret)
   (timbre/spy (type ?ret))
   (timbre/spy ?error))))

(defn get-name []
 (.getName CalendarModule))

(defn get-constants []
 (.getConstants CalendarModule))


;ObjC, listeners
;-----------------------------------------------------------------
(def calender-listener
 (fn [calendar-listener-ret]
  (timbre/spy
   (b/->clj calendar-listener-ret))))

(def ^js CalendarEmitter (NativeEventEmitter. CalendarModule))

(defn add-listener [event-name f]
 (.addListener CalendarEmitter event-name f))

(defn remove-listener
 "Removes a SPECIFIC listener for an event-name.
  IMPORTANT: Original callback fn must be provided."
 [event-name f]
 (.removeListener CalendarEmitter event-name f))

(defn remove-all-listeners [event-name]
 (.removeAllListeners CalendarEmitter event-name))
