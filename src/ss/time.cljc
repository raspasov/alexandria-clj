(ns ss.time
  (:require [tick.timezone]
            [tick.core :as t]))


(defn timestamp []
  #?(:cljs (js/Date.now)
     :clj  (System/currentTimeMillis)))


(defn timestamp-tz []
 {:time/timestamp (timestamp) :time/tz (str (t/zone))})


(defn inst->timestamp [inst]
  (long inst))


(defn timestamp->zoned-date-time
  ([timestamp]
   (timestamp->zoned-date-time timestamp (t/zone)))
  ([timestamp tz]
   (-> timestamp
       (t/instant)
       (t/zoned-date-time)
       (t/in tz))))


(defn time-map->zoned-date-time
 "Helper for time represented with a map like:
  {:time/timestamp 1620994460453, :time/tz \"America/Los_Angeles\"}"
 [{:time/keys [timestamp tz]}]
 (timestamp->zoned-date-time timestamp tz))


(defn date-time->timestamp [dt]
  (-> dt
      (t/inst)
      (t/long)))


(defn countdown-generic
  "Gives a map of the countdown with units of time as keys."
 ([end-instant]
  (countdown-generic (t/instant) end-instant false))
 ([start-instant end-instant always-show?]
  (let [duration (t/duration
                  {:tick/beginning start-instant
                   :tick/end       end-instant})
        weeks    (long (t/divide duration (t/new-duration 7 :days)))
        days     (t/days (t/- duration
                          (t/new-duration (* weeks 7) :days)))
        hours    (t/hours (t/- duration
                           (t/new-duration (+ days (* weeks 7)) :days)))
        minutes  (t/minutes (t/- duration
                             (t/new-duration (+ days (* weeks 7)) :days)
                             (t/new-duration hours :hours)))
        seconds  (t/seconds (t/- duration
                             (t/new-duration (+ days (* weeks 7)) :days)
                             (t/new-duration hours :hours)
                             (t/new-duration minutes :minutes)))
        millis   (t/millis (t/- duration
                            (t/new-duration (+ days (* weeks 7)) :days)
                            (t/new-duration hours :hours)
                            (t/new-duration minutes :minutes)
                            (t/new-duration seconds :seconds)))]
   (if (or always-show? (t/< (t/instant) end-instant))
    {:counting?    true
     :weeks        weeks
     :days         days
     :hours        hours
     :minutes      minutes
     :seconds      seconds
     :milliseconds millis}
    {:counting? false}))))


(defn today-at-midnight-in-tz
  ([] (today-at-midnight-in-tz (t/zone)))
  ([tz] (->
          (t/today)
          (t/at (t/midnight))
          (t/in (str tz)))))


(defn tomorrow-at-midnight-in-tz
  ([] (tomorrow-at-midnight-in-tz (t/zone)))
  ([tz] (->
          (t/tomorrow)
          (t/at (t/midnight))
          (t/in (str tz)))))








(comment
 ;EXAMPLES

 ;Range of timestamps
 (let [now (t/now)
       now' (t/truncate now :minutes)]
  (t/range
   (t/beginning now')
   (t/end (t/instant (t/at (t/tomorrow) (t/midnight))))
   (t/new-duration 1 :minutes)))

 )
