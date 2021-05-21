(ns ss.react-native-lib.webview
 (:require [react-native-webview :as -webview]
           [ss.react.core :as rc]))

(def ^js/Object webview -webview)

(def view (partial rc/create-element-js (.-WebView webview)))
