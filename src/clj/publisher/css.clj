(ns publisher.css
  (:refer-clojure :exclude [+ - * /])
  (:require [garden.core :refer [css]]
            [garden.units :as u :refer [px pt]]
            [garden.color :as color :refer [hsl rgb]]
            [garden.arithmetic :refer [+ - * /]]))

(css
  {:output-to "resources/public/app.css"}
  [:body {:font-family ["Helvetica" "sans-serif"]
          :font-size (px 14)
          :line-height 1.2}]
  [:ul {:padding 0
        :list-style :none
        :float :left
        :width (px 120)
        :margin 0}
   [:li {:padding (px 6)
         :cursor :pointer}]
   [:li.selected {:background :blue
                  :color :white}]])
