(ns ss.react-native-lib.mixpanel
 (:require [mixpanel-react-native :as -mxp]
           [clojure.core.async :as a]
           [taoensso.timbre :as timbre]
           [cljs-bean.core :as b]))

(def ^js/Object mxp -mxp)

(def ^js/Object Mixpanel (.-Mixpanel mxp))

(defonce *mxp-obj (atom nil))
(defonce mixpanel-ready-ch (a/promise-chan))

(defn ^js/Object get-mxp-obj []
 @*mxp-obj)

(defn identify [user-id]
 (a/go
  (a/<! mixpanel-ready-ch)
  (let [^js/Object mxp-obj (get-mxp-obj)]
   (.identify mxp-obj user-id))))


(defn init [token]
 (a/go
  (if @*mxp-obj
   true
   (let [[?mixpanel-obj ?mixpanel-error] (a/<! (.init Mixpanel token))
         ok? (nil? ?mixpanel-error)]
    (when ?mixpanel-error
     (timbre/spy ?mixpanel-error))
    (when ok?
     (reset! *mxp-obj ?mixpanel-obj)
     (a/put! mixpanel-ready-ch :ready/mixpanel))
    (timbre/info "Mixpanel init ok?" ok?)
    ok?))))

(defn flush! []
 (a/go
  (a/<! mixpanel-ready-ch)
  (let [^js/Object mxp-obj (get-mxp-obj)]
   (.flush mxp-obj))))

(defn track
 ([event-name data flush?]
  (a/go
   (a/<! mixpanel-ready-ch)
   (let [^js/Object mxp-obj (get-mxp-obj)]
    (timbre/info "Mixpanel track" event-name data)
    (.track mxp-obj (name event-name) (b/->js data))
    (when flush?
     (.flush mxp-obj))))))



