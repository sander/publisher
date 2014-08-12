(ns publisher.html
  (:use hiccup.page))

(spit "resources/public/index.html"
  (html5
    [:head
     [:meta {:charset "utf-8"}]
     (include-css "app.css")]
    [:body
     [:div#app]
     (include-js "react-0.11.1.js"
                 "out/goog/base.js"
                 "publisher.js")
     [:script "goog.require('publisher.core')"]
     [:script (cemerick.austin.repls/browser-connected-repl-js)]]))

(println "built html")
