(defproject publisher "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2280"]
                 [com.cognitect/transit-cljs "0.8.178"]
                 [com.cognitect/transit-clj "0.8.247"]
                 [om "0.7.1"]
                 [org.clojure/core.async "0.1.319.0-6b1aca-alpha"]
                 [garden "1.2.1"]
                 [lein-garden "0.1.9"]
                 [hiccup "1.0.5"]
                 [ring "1.2.1"]
                 [compojure "1.1.6"]
                 [enlive "1.1.5"]
                 [figwheel "0.1.3-SNAPSHOT"]
                 [om-sync "0.1.1"]
                 [http-kit "2.1.16"]
                 [compojure "1.1.8"]
                 [aleph "0.3.3"]
                 [ring-transit "0.1.2"]]

  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]
            [com.cemerick/austin "0.1.4"]
            [lein-figwheel "0.1.3-SNAPSHOT"]
            [cider/cider-nrepl "0.7.0"]]
  ;:repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :repl-options {:init-ns publisher.core}

  :figwheel {:css-dirs ["resources/public"]}

  :eval-in :repl

  :garden {:builds [{:stylesheet publisher.core/style
                     :compiler {:output-to "out/style.css"
                                :pretty-print? false}}]}

  ;:hooks [leiningen.garden]

  :source-paths ["src/clj" "src/cljs" "src/node"]

  :cljsbuild {
    :builds [{:id "publisher"
              :source-paths ["src/cljs"]
              :compiler {
                :output-to "resources/public/publisher.js"
                :output-dir "resources/public/out"
                :optimizations :none
                :source-map true}}
             {:id "server"
              :source-paths ["src/node"]
              :compiler {
                         :target :nodejs
                         :output-to "server.js"
                         :output-dir "out-node"
                         :optimizations :simple
                         :pretty-print true}}]})
