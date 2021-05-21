(ns ss.react-html.core
  (:require [react]
            [taoensso.timbre :as timbre]
            [goog.object :as obj]
            [ss.react.core :as rc]))

(def div (partial rc/create-element-js "div"))

(def img (partial rc/create-element-js "img"))

(def iframe (partial rc/create-element-js "iframe"))
