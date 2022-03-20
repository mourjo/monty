(ns monty.experiment)

(defn door-selections
  [doors reveals]
  {:pre [(< reveals (dec doors))]}
  (let [all-doors (range doors)
        winning-door (rand-int doors)
        contestant-first-selection (rand-int doors)
        host-selections (->> all-doors
                             shuffle
                             (remove (set [winning-door contestant-first-selection]))
                             (take reveals)
                             set)
        chosen-already? (conj host-selections contestant-first-selection)
        contestant-switched-selection (->> all-doors
                                           shuffle
                                           (remove chosen-already?)
                                           first)]
    {:first-selection contestant-first-selection
     :winning-door winning-door
     :host-selections host-selections
     :switched-selection contestant-switched-selection}))


(defn run-experiment
  [doors reveals]
  (let [{:keys [first-selection switched-selection winning-door]}
        (door-selections doors reveals)]
    [(if (= winning-door first-selection) 1 0)
     (if (= winning-door switched-selection) 1 0)]))


(defn compute-win-percent
  [experiments doors reveals]
  (let [[first-selection-wins
         second-selection-wins] (reduce (fn [[prev-a prev-b] [a b]]
                                          [(+ a prev-a) (+ b prev-b)])
                                        [0 0]
                                        (repeatedly experiments #(run-experiment doors reveals)))] 
    [(* 100 (double (/ first-selection-wins experiments)))
     (* 100 (double (/ second-selection-wins experiments)))]))


(defn monte-carlo-plot
  [experiments doors reveals & {:keys [width height]
                                 :or {width 200 height 200}}]
  (let [data (mapcat (fn [num-experiment]
                       (let [[no-switch-wins switch-wins] (compute-win-percent num-experiment doors reveals)]
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
               (let [[first-selection-percent second-selection-percent] (compute-win-percent experiments doors reveals)]
                 {:win-percent (if switch? second-selection-percent first-selection-percent)
                  :number-of-doors doors
                  :number-of-reveals-by-host reveals}))]
    {:$schema "https://vega.github.io/schema/vega-lite/v5.json",
     :data {:values data},
     :mark "rect",
     :title (format "%d experiments, %s" experiments (if switch? "when switching" "when not switching"))
     :width width,
     :height height,
     :encoding
     {:x {:bin {:maxbins max-doors}, :field "number-of-doors", :type "quantitative"},
      :y {:bin {:maxbins max-doors}, :field "number-of-reveals-by-host", :type "quantitative"},
      :color {:aggregate "avg", :field "win-percent" :type "quantitative"}},
     :config {:view {:stroke "transparent"}}}))


