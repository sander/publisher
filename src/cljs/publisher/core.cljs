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
(def app-state (atom {:docs []}))

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

(defn item-view [item owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [select selected]}]
      (dom/li #js {:className (if (= selected item) "selected" "")
                   :onClick #(put! select item)}
              (:name item)))))

(defn display [doc owner]
  (reify
    om/IInitState
    (init-state [_]
      (print "initting" (:name doc))
      {:doc-content "not loaded yet"})
    om/IWillMount
    (will-mount [_]
      (print "doccing" (:name doc))
      (go (let [content (<! (xhr-get-text "docs"))]
            (om/set-state! owner :doc-content content))))
    om/IRenderState
    (render-state [this {:keys [doc-content]}]
      (dom/div nil
               (dom/p nil (:name doc))
               (dom/p nil doc-content)))))

; nicer to have "opened" as app state b/c that remains after reload
; not

; seems that with cursors, it still makes sense to have the docs list and doc selection under one cursor handled by an item-list component

; can opened as a chan be part of app-state?

(defn widget [app owner]
  (reify
    om/IInitState
    (init-state [_] {:open (chan)})
    om/IWillMount
    (will-mount [_]
      (let [open (om/get-state owner :open)]
        (go-loop []
                 (let [doc (<! open)]
                   ;(om/transact! app :opened (fn [] doc))
                   (om/set-state! owner :opened doc)
                   (recur)))))
    om/IRenderState
    (render-state [this {:keys [open opened]}]
      (dom/div nil
               (apply dom/ul nil
                      (om/build-all item-view (docs app)
                                    {:init-state {:select open}
                                     :state {:selected opened}}))
               (om/build display opened {:init-state {:doc-content "hoi"}})
               
               ))))

(om/root widget app-state
         {:target (. js/document (getElementById "app"))})

(go (swap! app-state assoc :docs (<! (get-docs))))
