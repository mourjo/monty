(ns monty.core
  (:require [oz.core :as oz])
  (:gen-class))

(defn experiment
  [n]
  (let [winning-door (rand-int n)
        contestant-first-selection (rand-int n)
        host-selection (->> (repeatedly #(rand-int n))
                            (remove (set [contestant-first-selection winning-door]))
                            first)
        contestant-switched-selection (->> (repeatedly #(rand-int n))
                                           (remove (set [host-selection contestant-first-selection]))
                                           first)]
    [(if (= winning-door contestant-first-selection) 1 0)
     (if (= winning-door contestant-switched-selection) 1 0)]))


(defn repeated-experiment
  [n-experiments n-doors]
  (let [[first-selection-wins
         second-selection-wins] (reduce (fn [[prev-a prev-b] [a b]]
                                          [(+ a prev-a) (+ b prev-b)])
                                        [0 0]
                                        (repeatedly n-experiments #(experiment n-doors)))] 
    [(* 100 (double (/ first-selection-wins n-experiments)))
     (* 100 (double (/ second-selection-wins n-experiments)))]))


(defn monte-carlo
  [n-experiments n-doors]
  (mapcat (fn [num-experiment]
            (let [[no-switch-wins switch-wins] (repeated-experiment num-experiment n-doors)]
              [{:num-experiments num-experiment :type :no-switch :win-percent no-switch-wins}
               {:num-experiments num-experiment :type :switch :win-percent switch-wins}]))
          (range 1 (inc n-experiments))))


(defn -main
  [& args]
  (let [expt-150-3 {:data {:values (monte-carlo 150 3)}
                    :width 600
                    :height 600
                    :encoding {:x {:field "num-experiments" :type "quantitative"}
                               :y {:field "win-percent" :type "quantitative"}
                               :color {:field "type" :type "nominal"}}
                    :mark "line"}

        expt-2000-3 {:data {:values (monte-carlo 2000 3)}
                     :width 600
                     :height 600
                     :encoding {:x {:field "num-experiments" :type "quantitative"}
                                :y {:field "win-percent" :type "quantitative"}
                                :color {:field "type" :type "nominal"}}
                     :mark "line"}

        expt-2000-5 {:data {:values (monte-carlo 2000 5)}
                     :width 600
                     :height 600
                     :encoding {:x {:field "num-experiments" :type "quantitative"}
                                :y {:field "win-percent" :type "quantitative"}
                                :color {:field "type" :type "nominal"}}
                     :mark "line"}]
    (oz/view!
     [:div
      [:h1 "150 Experiments with 3 Doors"]
      [:vega-lite expt-150-3]
      [:h1 "2000 Experiments with 3 Doors"]
      [:vega-lite expt-2000-3]
      [:h1 "2000 Experiments with 5 Doors"]
      [:vega-lite expt-2000-5]])))
