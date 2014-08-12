(ns publisher.server
  (:require [cljs.nodejs :as node]
            [cognitect.transit :as t]))
(node/enable-util-print!)

(def express (node/require "express"))
(def fs (node/require "fs"))
(def morgan (node/require "morgan"))

(def port 3000)
(def writer-path "/Users/sander/Library/Mobile Documents/27N4MQEA55~pro~writer/Documents")
(def skipped #{".DS_Store"})
(def w (t/writer :json))

(defn -handle-docs [req res]
  (letfn [(rdir [path cb]
                (letfn [(read [prefix paths result cb]
                              (if (empty? paths)
                                (cb result)
                                (let [name (first paths)
                                      path (str prefix name)]
                                  (.readdir fs path
                                            #(read prefix (rest paths)
                                                   (conj result [name (apply vector (remove skipped %2))])
                                                   cb)))))]
                  (.readdir fs path (fn [_ files]
                                      (read (str path "/") (remove skipped files) [] cb)))))]
    (rdir writer-path #(.end res (t/write w %)))))

(defn -main [& args]
  (doto (express)
    (.use (morgan "dev"))
    (.use ((aget express "static") "resources/public"))
    (.get "/docs" -handle-docs)
    (.listen port (fn [] (println (str "server on port " port "!"))))))

(set! *main-cli-fn* -main)
