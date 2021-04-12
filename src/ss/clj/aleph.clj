(ns ss.clj.aleph
  (:require [clojure.java.io :as io]
            [buddy.auth.middleware :as buddy-auth-middleware]
            [manifold.deferred :as d]
            clojure.reflect)
  (:import (java.util.zip GZIPOutputStream)
           (java.io InputStream
                    OutputStream
                    Closeable
                    File
                    PipedInputStream
                    PipedOutputStream)))

; only available on JDK7
(def ^:private flushable-gzip?
  (delay (->> (clojure.reflect/reflect GZIPOutputStream)
              :members
              (some (comp '#{[java.io.OutputStream boolean]} :parameter-types)))))

; only proxying here so we can specialize io/copy (which ring uses to transfer
; InputStream bodies to the servlet response) for reading from the result of
; piped-gzipped-input-stream
(defn- piped-gzipped-input-stream*
  []
  (proxy [PipedInputStream] []))

; exactly the same as do-copy for [InputStream OutputStream], but
; flushes the output on every chunk; this allows gzipped content to start
; flowing to clients ASAP (a reasonable change to ring IMO)
(defmethod @#'io/do-copy [(class (piped-gzipped-input-stream*)) OutputStream]
  [^InputStream input ^OutputStream output opts]
  (let [buffer (make-array Byte/TYPE (or (:buffer-size opts) 1024))]
    (loop []
      (let [size (.read input buffer)]
        (when (pos? size)
          (do (.write output buffer 0 size)
              (.flush output)
              (recur)))))))

(defn piped-gzipped-input-stream [in]
  (let [pipe-in (piped-gzipped-input-stream*)
        pipe-out (PipedOutputStream. pipe-in)]
    ; separate thread to prevent blocking deadlock
    (future
      (with-open [out (if @flushable-gzip?
                        (GZIPOutputStream. pipe-out true)
                        (GZIPOutputStream. pipe-out))]
        (if (seq? in)
          (doseq [string in]
            (io/copy (str string) out)
            (.flush out))
          (io/copy in out)))
      (when (instance? Closeable in)
        (.close ^Closeable in)))
    pipe-in))


(defn gzipped-response [resp]
  (-> resp
      (update-in [:headers]
                 #(-> %
                      (assoc "Content-Encoding" "gzip")
                      (dissoc "Content-Length")))
      (update-in [:body] piped-gzipped-input-stream)))


(defn wrap-gzip [handler]
  (fn [req]
    (d/let-flow
      [{:keys [body status] :as resp} (handler req)]
      (if (and (= status 200)
               (not (get-in resp [:headers "Content-Encoding"]))
               (or
                 (and (string? body) (> (count body) 200))
                 (and (seq? body) @flushable-gzip?)
                 (instance? InputStream body)
                 (instance? File body)))
        (let [accepts (get-in req [:headers "accept-encoding"] "")
              match   (re-find #"(gzip|\*)(;q=((0|1)(.\d+)?))?" accepts)]
          (if (and match (not (contains? #{"0" "0.0" "0.00" "0.000"}
                                         (match 3))))
            (gzipped-response resp)
            resp))
        resp))))

(defn wrap-authentication
  "Ring middleware that enables authentication for your ring
  handler. When multiple `backends` are given each of them gets a
  chance to authenticate the request."
  [handler & backends]
  (fn [request]
    (d/let-flow
      [resp (handler (apply buddy-auth-middleware/authentication-request request backends))]
      resp)))

