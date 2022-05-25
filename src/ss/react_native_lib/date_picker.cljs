(ns ss.react-native-lib.date-picker
 (:require [react-native-date-picker :as -picker]
           [ss.react.core :as rc]))


(def ^js DatePicker -picker)
(def date-picker (partial rc/create-element-js (.-default DatePicker)))
