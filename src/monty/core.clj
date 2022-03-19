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
  (reduce (fn [[prev-a prev-b] [a b]]
          [(+ a prev-a) (+ b prev-b)])
        [0 0]
        (repeatedly n-experiments #(experiment n-doors))))


(defn monte-carlo
  [n-experiments n-doors]
  (mapcat (fn [num-experiment]
            (let [[no-switch-wins switch-wins] (repeated-experiment num-experiment n-doors)]
              [{:num-experiments num-experiment :type :no-switch :wins no-switch-wins}
               {:num-experiments num-experiment :type :switch :wins switch-wins}]))
          (range 1 (inc n-experiments))))


(defn -main
  [& args]
  (let [s {:data {:values (monte-carlo 5000 3)}
           :width 800
           :height 800
           :encoding {:x {:field "num-experiments" :type "quantitative"}
                      :y {:field "wins" :type "quantitative"}
                      :color {:field "type" :type "nominal"}}
           :mark "line"}]
    (oz/view! s)))
