(ns ax.clj.filesystem
  (:require [clojure.java.io :as io]))


(defn list-directory
  ([^String path]
   (list-directory path (map identity)))
  ([^String path xf]
   (sequence
     (comp
       ;remove DS_Store, directory itself
       (remove (fn [file] (= ".DS_Store" (.getName file))))
       xf)
     ;all the files
     (file-seq (io/file path)))))


(defn copy-file [source-path dest-path]
  (io/copy (io/file source-path) (io/file dest-path)))
