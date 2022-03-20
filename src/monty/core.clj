(ns monty.core
  (:require [oz.core :as oz]
            [monty.experiment :as me])
  (:gen-class))


(defn line-plot
  [experiments doors reveals & {:keys [width height]
                                 :or {width 200 height 200}}]
  (let [data (mapcat (fn [num-experiment]
                       (let [[no-switch-wins switch-wins] (me/compute-win-percent num-experiment doors reveals)]
                         [{:num-experiments num-experiment :type :no-switch :win-percent no-switch-wins}
                          {:num-experiments num-experiment :type :switch :win-percent switch-wins}]))
                     (range 1 (inc experiments)))]
    {:data {:values data}
     :width width
     :height height
     :title (format "%d experiments, %d doors, %d reveals" experiments doors reveals)
     :encoding {:x {:field "num-experiments" :type "quantitative"}
                :y {:field "win-percent" :type "quantitative"}
                :color {:field "type" :type "nominal"}}
     :mark "line"}))


(defn heat-map-plot
  [experiments max-doors switch? & {:keys [width height]
                                    :or {width 400 height 400}}]
  {:pre [(<= 3 max-doors)]}
  (let [data (for [doors (range 3 (inc max-doors))
                   reveals (range 1 (inc (- doors 2)))]
               (let [[first-selection-percent second-selection-percent] (me/compute-win-percent experiments doors reveals)]
                 {:win-percent (if switch? second-selection-percent first-selection-percent)
                  :number-of-doors doors
                  :number-of-reveals-by-host reveals}))]
    {:$schema "https://vega.github.io/schema/vega-lite/v5.json",
     :data {:values data},
     :mark "rect",
     :title (format "%d experiments (each bin), %s" experiments (if switch? "when switching" "when not switching"))
     :width width,
     :height height,
     :encoding
     {:x {:bin {:maxbins max-doors}, :field "number-of-doors", :type "quantitative"},
      :y {:bin {:maxbins max-doors}, :field "number-of-reveals-by-host", :type "quantitative"},
      :color {:aggregate "average", :field "win-percent" :type "quantitative"}},
     :config {:view {:stroke "transparent"}}}))


(defn render!
  [experiments]
  (oz/view!
   [:div
    [:h4 "How does win percent change with number of doors?"]
    [:p "When the number of doors increases but the number of reveals remains 1, the win-percent quickly drops."]
    [:div {:style {:display "flex" :flex-direction "row"}}
     [:vega-lite (line-plot experiments 3 1)]
     [:vega-lite (line-plot experiments 5 1)]
     [:vega-lite (line-plot experiments 10 1)]]

    [:hr]

    [:h4 "What if there are 100 doors and many are revealed?"]
    [:p "Intuitively, switching after the reveal seems like a better choice if out of many
    doors, many are revealed. Suppose there are 100 doors and the contestant chooses 1,
    but then the host reveals 98 of the other doors which are definitely not the answer."]
    [:div {:style {:display "flex" :flex-direction "row"}}
     [:vega-lite (line-plot experiments 100 70)]
     [:vega-lite (line-plot experiments 100 80)]
     [:vega-lite (line-plot experiments 100 90)]]

    [:div {:style {:display "flex" :flex-direction "row"}}
     [:vega-lite (line-plot experiments 100 96)]
     [:vega-lite (line-plot experiments 100 97)]
     [:vega-lite (line-plot experiments 100 98)]]

    [:hr]

    [:h4 "How does win percent change with reveals?"]
    [:p "When the contenstant switches after the host's reveal, there is a higher chance
    of winning for the contenstant when significant number of doors were
    revealed. However, if the contestant does not switch, the chance is much lower --
    uniformly distributed. Therefore, chance of winning is never worse when switching."]
    [:div {:style {:display "flex" :flex-direction "row"}}
     [:vega-lite (heat-map-plot experiments 100 false)]
     [:vega-lite (heat-map-plot experiments 100 true)]]]))


(defn -main
  [& args]
  (oz/start-server!)
  (-> (System/getenv "NUM_EXPERIMENTS")
      (or "100")
      Integer/parseInt
      render!))
