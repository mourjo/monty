(ns monty.core
  (:require [oz.core :as oz]
            [monty.experiment :as me])
  (:gen-class))


(defn render!
  []
  (oz/view!
   [:div
    [:div {:style {:display "flex" :flex-direction "row"}}
     [:vega-lite (me/monte-carlo 150 3 1)]
     [:vega-lite (me/monte-carlo 2000 3 1)]
     [:vega-lite (me/monte-carlo 2000 5 1)]]

    [:hr]
    
    [:div {:style {:display "flex" :flex-direction "row"}}
     [:vega-lite (me/monte-carlo 500 100 70)]
     [:vega-lite (me/monte-carlo 500 100 80)]
     [:vega-lite (me/monte-carlo 500 100 90)]]

    [:div {:style {:display "flex" :flex-direction "row"}}
     [:vega-lite (me/monte-carlo 500 100 96)]
     [:vega-lite (me/monte-carlo 500 100 97)]
     [:vega-lite (me/monte-carlo 500 100 98)]]]))


(defn -main
  [& args]
  (oz/start-server!)
  (render!))
