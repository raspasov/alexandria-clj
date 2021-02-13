(ns ax.time
  (:require [tick.timezone]
            [tick.alpha.api :as t]))


(defn timestamp []
  #?(:cljs (js/Date.now)
     :clj  (System/currentTimeMillis)))


(defn inst->timestamp [inst]
  (long inst))


(defn timestamp->zoned-date-time [timestamp tz]
  (-> timestamp
      (t/instant)
      (t/zoned-date-time)
      (t/in tz)))


(defn countdown-generic
  "Gives a map of the countdown with units of time as keys."
  [end-time]
  (let [duration (t/duration
                   {:tick/beginning (t/instant)
                    :tick/end       end-time})
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
    (if (t/< (t/instant) end-time)
      {:counting     true
       :weeks        weeks
       :days         days
       :hours        hours
       :minutes      minutes
       :seconds      seconds
       :milliseconds millis}
      {:counting false})))


(defn today-at-midnight-in-tz
  ([] (today-at-midnight-in-tz (t/zone)))
  ([tz] (->
          (t/today)
          (t/at (t/midnight))
          (t/in (str tz)))))


(defn day-of-week-xf [day-of-week]
  (comp
    (map (juxt t/day-of-week identity))
    (filter #(= (t/day-of-week day-of-week) (first %)))
    (take 1)))


(defn prev-day-of-week
  "Walks backward and finds the desired day-of-week"
  [day-of-week]
  (let [today-midnight (today-at-midnight-in-tz)]
    (first
      (sequence
        (day-of-week-xf day-of-week)
        (t/range
          (t/beginning (t/- today-midnight (t/new-period 7 :days)))
          today-midnight
          (t/new-period 1 :days))))))


(defn next-day-of-week
  "Walks forward and finds the desired day-of-week"
  [day-of-week]
  (let [today-midnight (today-at-midnight-in-tz)]
    (first
      (sequence
        (day-of-week-xf day-of-week)
        (t/range
          today-midnight
          (t/end (t/+ today-midnight (t/new-period 7 :days)))
          (t/new-period 1 :days))))))
