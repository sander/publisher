(ns publisher.core
  (:require [cemerick.austin]
            [cemerick.austin.repls]))

(defn build-html []
  (require 'publisher.html :reload))
(defn build-css []
  (require 'publisher.css :reload))
(defn build []
  (build-html)
  (build-css))

;(cemerick.austin/repl-env)
(def repl-env (reset! cemerick.austin.repls/browser-repl-env
                      (cemerick.austin/repl-env)))
;(cemerick.austin.repls/browser-connected-repl-js)
;(defn init-repl []
;  (cemerick.austin.repls/cljs-repl 
;  (cemerick.austin.repls/cljs-repl repl-env))
;:cljs/quit
;(js/Date)
;Piggieback
;(js/alert "test")
;:cljs/quit

;(build-css)

;(build-html)
(build-html)
