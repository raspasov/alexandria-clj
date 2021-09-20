(ns ss.react-native-lib.health
 (:require
  [cljs-bean.core :as b]
  [clojure.core.async :as a]
  [react-native-health :as -health]
  [taoensso.timbre :as timbre]
  [tick.alpha.api :as t]))


(def ^js health -health)

(defn is-available []
 (.isAvailable health
  (fn [err available?]
   (timbre/spy [err available?]))))


(def permissions-1
 (b/->js
  {:permissions {:read [:HeartRate]}}))


(defn init-health-kit [permissions]
 (.initHealthKit health
  permissions
  (fn [?error]
   (timbre/spy ?error)
   (timbre/info "Can now read/write health kit"))))


(defonce *heart-rate-results (atom nil))

(def options-1
 (b/->js
  {:startDate (.toISOString
               (t/inst
                (t/at (t/new-date 2021 8 30) "00:00")))}))


(defn date-string->timestamp [s]
 (long (new js/Date. s)))


(defn get-heart-rate-samples [options]
 (.getHeartRateSamples health
  options
  (fn [?error results]
   (timbre/spy ?error)
   (let [results (b/->clj results)]
    (reset! *heart-rate-results results)
    (timbre/spy (first results))
    (cljs.pprint/pprint
     (mapv (juxt :value :startDate :endDate) results))))))


(defn repl-heart-rate-results []
 (into (sorted-map)
  (map (fn [m] [(-> m :startDate (date-string->timestamp)) (:value m)]))
  @*heart-rate-results))

(comment
 (cljs.pprint/pprint
  (b/->clj
   (.. health -Constants -Permissions -HeartRate))))
