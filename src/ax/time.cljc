(ns ax.time
  (:require [tick.timezone]
            [tick.alpha.api :as t]))


(defn timestamp []
  #?(:cljs (js/Date.now)
     :clj  (System/currentTimeMillis)))


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


(defn date-time->timestamp [dt]
  (-> dt
      (t/inst)
      (t/long)))


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
      {:counting?     true
       :weeks        weeks
       :days         days
       :hours        hours
       :minutes      minutes
       :seconds      seconds
       :milliseconds millis}
      {:counting? false})))


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


(defn day-of-week-xf [day-of-week]
  (comp
    (map (juxt t/day-of-week identity))
    (filter #(= (t/day-of-week day-of-week) (first %)))
    (take 1)))


(defn prev-day-of-week
  "Walks backward and finds the desired day-of-week (inclusive today)"
  [day-of-week]
  (let [today-midnight (today-at-midnight-in-tz)
        tomorrow-midnight        (t/+ today-midnight (t/new-period 1 :days))
        tomorrow-midnight-7-days (t/- tomorrow-midnight (t/new-period 7 :days))
        [_ ret]
        (first
          (sequence
            (comp
              (day-of-week-xf day-of-week)
              #_(map t/day-of-week))
            (t/range
              tomorrow-midnight-7-days
              tomorrow-midnight
              (t/new-period 1 :days))))]
    ret))


(defn next-day-of-week
  "Walks forward and finds the desired day-of-week (exclusive today)"
  [day-of-week]
  (let [today-midnight           (today-at-midnight-in-tz)
        tomorrow-midnight        (t/+ today-midnight (t/new-period 1 :days))
        tomorrow-midnight+7-days (t/+ tomorrow-midnight (t/new-period 7 :days))
        [_ ret] (first
                  (sequence
                    (day-of-week-xf day-of-week)
                    (t/range
                      (t/beginning tomorrow-midnight)
                      (t/end tomorrow-midnight+7-days)
                      (t/new-period 1 :days))))]
    ret))


(defn weekly-chart-timestamps [day-of-week-start]
  (let [t1 (prev-day-of-week day-of-week-start)
        t2 (t/>> t1 (t/+ (t/new-period 1 :weeks)
                         (t/new-period 1 :days)))]
    (partition
      2 1
      (sequence
        (comp
          (map (juxt (comp str t/day-of-week) identity)))
        (t/range
          t1
          t2
          (t/new-period 1 :days))))))
