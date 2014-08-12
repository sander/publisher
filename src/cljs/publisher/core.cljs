(ns publisher.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
                   ;[publisher.macros :refer [defnas]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cognitect.transit :as t]
            [cljs.core.async :as async :refer [put! close! chan <!]]
            [clojure.browser.repl]
            [figwheel.client :as fw :include-macros true]))

; features:
; - compile to pdf with a html/js/css template
; - quick lookup
; - create outline, put into whole doc

(enable-console-print!)
(fw/watch-and-reload :websocket-url "ws://localhost:3449/figwheel-ws")

(def r (t/reader :json))
(def app-state (atom {:docs []
                      :selected [nil]
                      :content ["not loaded yet"]
                      :content-loaded [false]}))

(defn concatv [vs] (vec (apply concat vs)))
(defrecord Doc [folder name])
(defn docs [state]
  (concatv (mapv (fn [[fname docs]] (mapv #(->Doc fname %) docs))
                 (:docs state))))

(defn async-fn [f]
  (fn [& args]
    (let [out (chan)
          return #(do (put! out %) (close! out))]
      (apply f return args)
      out)))
(def xhr-get-text
  (async-fn (fn [return path]
              (doto (js/XMLHttpRequest.)
                (.addEventListener "load" #(return (.-target.responseText %)) false)
                (.open "get" path true)
                (.send)))))
(defn get-docs [] (go (t/read r (<! (xhr-get-text "docs")))))

(defn item-view [{:keys [item selected content-loaded]} _]
  (om/component
    (dom/li #js {:className (if (= (selected 0) item) "selected" "")
                 :onClick (fn [_]
                            (om/update! selected [0] item)
                            (om/update! content-loaded [0] false))}
            (:name item))))

(defn display [{:keys [doc content content-loaded]} _]
  (om/component
    (when (and doc (not (content-loaded 0)))
      (go (let [new-content (<! (xhr-get-text "docs"))]
            (om/update! content [0] (str (:name @doc) " *** " new-content))
            (om/update! content-loaded [0] true))))
    (dom/div nil
             (dom/p nil (:name doc))
             (dom/p nil (content 0)))))

(defn widget [app owner]
  (om/component
    (dom/div nil
             (apply dom/ul nil
                    (mapv #(om/build item-view
                                     {:item %
                                      :selected (:selected app)
                                      :content-loaded (:content-loaded app)})
                          (docs app)))
             (om/build display {:doc ((:selected app) 0)
                                :content (:content app)
                                :content-loaded (:content-loaded app)}))))

(om/root widget app-state
         {:target (. js/document (getElementById "app"))})

(go (swap! app-state assoc :docs (<! (get-docs))))
