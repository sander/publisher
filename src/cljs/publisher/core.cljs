(ns publisher.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
                   ;[publisher.macros :refer [defnas]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cognitect.transit :as t]
            [cljs.core.async :as async :refer [put! close! chan <!]]
            [clojure.browser.repl]
            [figwheel.client :as fw :include-macros true]
            [om-sync.core :refer [om-sync]]
            [om-sync.util]))

; features:
; - compile to pdf with a html/js/css template
; - quick lookup
; - create outline, put into whole doc

(enable-console-print!)
(fw/watch-and-reload :websocket-url "ws://localhost:3449/figwheel-ws")

(def r (t/reader :json))
(def w (t/writer :json))
(def app-state (atom {:docs []
                      :docs-loading true
                      :selected [nil]
                      :content ["not loaded yet"]
                      :content-loaded [false]}))

(defn concatv [vs] (vec (apply concat vs)))
;(defrecord Doc [folder name])
;(defn docs [state]
;  (:docs state))
  ;(concatv (mapv (fn [[fname docs]] (mapv #(->Doc fname %) docs))
  ;               (:docs state))))

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

(def xhr-put-transit
  (async-fn (fn [return path data]
              (doto (js/XMLHttpRequest.)
                (.addEventListener "load" #(return (t/read r (.-target.responseText %))) false)
                (.open "put" path true)
                (.setRequestHeader "Content-Type" "application/transit+json;charset=UTF-8")
                (.send (t/write w data))))))

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
                          (:docs app)))
             (om/build display {:doc ((:selected app) 0)
                                :content (:content app)
                                :content-loaded (:content-loaded app)}))))

(om/root widget app-state
         {:target (. js/document (getElementById "app"))})

;(def sapp-state (atom {}))
;
;(defn items-view [item _]
;  (om/component
;    (dom/div nil
;             "item["
;             item
;             "]")))
;
;(defn tx-tag [transaction]
;  :custom-tx-tag)
;
;(defn app-view [app owner]
;  (reify
;    om/IWillUpdate
;    (will-update [_ next-props next-state]
;      (when (:err-msg next-state)
;        (js/setTimeout #(om/set-state! owner :err-msg nil) 5000)))
;    om/IRenderState
;    (render-state [_ {:keys [err-msg]}]
;      (dom/div nil
;               ; om-sync {:url :coll} owner opts
;               (om/build om-sync (:items app)
;                         {:opts {:view items-view
;                                 :filter (comp #{:create :update :delete} tx-tag)
;                                 :id-key :some/id
;                                 :on-success (fn [res tx-data] (println res))
;                                 :on-error
;                                 (fn [err tx-data]
;                                   (reset! sapp-state (:old-state tx-data))
;                                   (om/set-state! owner :err-msg
;                                                  "Oops!"))}})
;               (when err-msg
;                 (dom/div nil err-msg))))))
;
;(let [tx-chan (chan)
;      tx-pub-chan (async/pub tx-chan (fn [_] :txs))]
;  (om-sync.util/edn-xhr
;    {:method :get
;     :url "/init"
;     :on-complete
;     (fn [res]
;       (reset! app-state res)
;       (om/root app-view sapp-state
;                {:target (. js/document (getElementById "app"))
;                 :shared {:tx-chan tx-pub-chan}
;                 :tx-listen
;                 (fn [tx-data root-cursor]
;                   (put! tx-chan [tx-data root-cursor]))}))}))

; make publisher.server implement /init, recv a state and send back a new state

; just check the intermediate om tutorial

;(go (swap! app-state assoc :docs (<! (get-docs))))

(go (reset! app-state (<! (xhr-put-transit "data" @app-state))))
;ask for docs by setting the value to loading
