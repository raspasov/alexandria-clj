(ns ss.react-native-lib.code-push)


(defn sync [sync-status-ch]
 #_(.sync
  code-push/CodePush
  (clj->js {:installMode (.. code-push/CodePush -InstallMode -IMMEDIATE)})
  (fn sync-status-change-cb [sync-status]
   (put! sync-status-ch sync-status))))
