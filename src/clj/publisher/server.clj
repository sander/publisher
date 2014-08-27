(ns publisher.server
  (:require [ring.util.response :refer [response]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.transit :refer
             [wrap-transit-body wrap-transit-response]]
            [compojure.core :refer [defroutes GET PUT POST]]
            [compojure.route :as route]))

(def port 3000)
(def writer-path "/Users/sander/Library/Mobile Documents/27N4MQEA55~pro~writer/Documents")
(def skipped #{".DS_Store"})

(defn docs []
  (response
    [["aap" ["noot" "mies"]]
     ["nog een aap" ["test" "testje"]]])) 

(defn get-docs []
  (->> writer-path
       (clojure.java.io/file)
       (file-seq)
       (filter #(.isFile %))
       (remove #(skipped (.getName %)))
       (map (fn [f]
              {:folder (.getName (.getParentFile f))
               :name (.getName f)}))))

(defn update-data [data]
  (response
    (cond
      (data :docs-loading) (assoc data
                                  :docs (get-docs)
                                  :docs-loading false)
      :else data)))

(defroutes routes
  (GET "/docs" [] (docs))
  (PUT "/data" {:keys [body]} (update-data body))
  (route/files "/" {:root "resources/public"}))

(def app (-> routes
             wrap-transit-body
             wrap-transit-response))

(defonce server
  (run-jetty #'app {:port port :join? false}))
