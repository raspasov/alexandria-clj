(ns ss.apple.verify-receipt
 (:require [ss.clj.aleph-client :as http-client]
           [cheshire.core :as cheshire]))


(def apple-verify-receipt-sandbox
 "https://sandbox.itunes.apple.com/verifyReceipt")
(def apple-verify-receipt-production
 "https://buy.itunes.apple.com/verifyReceipt")


(defn verify-receipt [{:keys [receipt-data password sandbox?]}]
 (let [{:keys [body status] :as resp}
       (http-client/post-clj-http
        apple-verify-receipt-sandbox
        {:form-params  {:password     password
                        :receipt-data receipt-data}
         :content-type :json})]
  (if (= 200 status)
   (let [{:keys [status]} (cheshire/parse-string body true)]
    (= 0 status))
   false)))


