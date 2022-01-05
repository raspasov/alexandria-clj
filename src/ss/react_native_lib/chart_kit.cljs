(ns ss.react-native-lib.chart-kit
  (:require [react-native-chart-kit :as -rnck]
            [ss.react.core :as rc]
            [tick.core :as t]
            [ss.time :as ax|t]))


(def ^js/Object rnck -rnck)


(def line-chart (partial rc/create-element-js (.-LineChart rnck)))


(defn exercise-sets-by-day-of-week [exercise-sets]
  (group-by
    (fn [{{:time/keys [timestamp tz]} :exercise.set/end-time}]
      (str
        (t/day-of-week
          (ax|t/timestamp->zoned-date-time timestamp tz))))
    exercise-sets))


(defn reps-by-day-of-week [grouped-exercise-sets]
  (into
    []
    (comp
      (take-while
        (fn [day-of-week]
          (let [tomorrow (t/day-of-week (t/tomorrow))]
            ;if tomorrow is Monday, take all the days
            (if (= tomorrow (t/day-of-week "MON"))
              true
              ;else figure out when to stop
              (not= day-of-week (t/day-of-week (t/tomorrow)))))))
      (map str)
      (map (fn [day-of-week]
             (let [exercise-sets (get grouped-exercise-sets day-of-week)]
               (transduce (map :exercise.set/rep-count) + exercise-sets)))))
    [(t/day-of-week "MON")
     (t/day-of-week "TUE")
     (t/day-of-week "WED")
     (t/day-of-week "THUR")
     (t/day-of-week "FRI")
     (t/day-of-week "SAT")
     (t/day-of-week "SUN")]))

#_(r/view
    {:style {:paddingVertical (dm/<> 25)
             :flex            1
             :justifyContent  "space-between"
             :alignItems      "center"}}



    (let [weekly-reps-data  (ax|n/running-total
                              (reps-by-day-of-week
                                (exercise-sets-by-day-of-week weekly-exercise-sets)))
          fill-up-n         (- 7 (count weekly-reps-data))
          weekly-reps-data' (concat weekly-reps-data (repeat fill-up-n nil))]
      (ax|rnck/line-chart
        {:data                 {:labels   ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]
                                :datasets [{:data        (repeat 7 total-reps)
                                            :strokeWidth 8}
                                           {:data        weekly-reps-data'
                                            :strokeWidth 3}]
                                :legend   ["Reps Goal"]}
         ;:withInnerLines       false
         ;:withOuterLines       false
         ;:withVerticalLabels false
         :fromZero             true
         :segments             5
         :withHorizontalLabels true
         :formatYLabel         (fn [x]
                                 (let [x' (js/Math.round (ax|n/string->number x))]
                                   x'))
         :width                (dm/ww)
         :height               220
         :chartConfig          {:backgroundGradientFrom        "black",
                                :backgroundGradientFromOpacity 1,
                                :backgroundGradientTo          "black",
                                :backgroundGradientToOpacity   1
                                :propsForLabels                {:fontWeight "bold"}
                                :propsForVerticalLabels        {:fontWeight "bold"
                                                                ;:fill       "red"
                                                                }
                                :propsForHorizontalLabels      {:fontWeight "bold"
                                                                ;:fill       "red"
                                                                }
                                :color                         (fn [] (clr/white))
                                :strokeWidth                   1
                                :barPercentage                 0.5,
                                :useShadowColorFromDataset     false}}))

    (r/touchable-opacity
      {:onPress (fn [_] (ax|vl/v-list-scroll-to-index (ax|state-fns/get-global-ref :main-ui-vlist) 1))}
      (r/text {:style (merge text-style-1 {:fontSize (dm/<> 30)})} "EXECUTE")))
