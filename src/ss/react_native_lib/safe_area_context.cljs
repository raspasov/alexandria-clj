(ns ss.react-native-lib.safe-area-context
 (:require [react-native-safe-area-context :as -safe-area-context]
           [ss.react.core :as rc]
           [cljs-bean.core :as b]))


(def ^js safe-area-context -safe-area-context)

(def safe-area-view (partial rc/create-element-js (.-SafeAreaView safe-area-context)))
(def safe-area-provider (partial rc/create-element-js (.-SafeAreaProvider safe-area-context)))

(defn use-safe-area-insets []
 (b/->clj (.useSafeAreaInsets safe-area-context)))
