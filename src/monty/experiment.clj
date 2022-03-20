(ns monty.experiment)

(defn door-selections
  [doors removals]
  {:pre [(< removals (dec doors))]}
  (let [all-doors (range doors)
        winning-door (rand-int doors)
        contestant-first-selection (rand-int doors)
        host-selections (->> all-doors
                             shuffle
                             (remove (set [winning-door contestant-first-selection]))
                             (take removals)
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
  [doors removals]
  (let [{:keys [first-selection switched-selection winning-door]}
        (door-selections doors removals)]
    [(if (= winning-door first-selection) 1 0)
     (if (= winning-door switched-selection) 1 0)]))


(defn compute-win-percent
  [experiments doors removals]
  (let [[first-selection-wins
         second-selection-wins] (reduce (fn [[prev-a prev-b] [a b]]
                                          [(+ a prev-a) (+ b prev-b)])
                                        [0 0]
                                        (repeatedly experiments #(run-experiment doors removals)))] 
    [(* 100 (double (/ first-selection-wins experiments)))
     (* 100 (double (/ second-selection-wins experiments)))]))


(defn monte-carlo
  [experiments doors removals & {:keys [width height]
                                 :or {width 200 height 200}}]
  (let [data (mapcat (fn [num-experiment]
                       (let [[no-switch-wins switch-wins] (compute-win-percent num-experiment doors removals)]
                         [{:num-experiments num-experiment :type :no-switch :win-percent no-switch-wins}
                          {:num-experiments num-experiment :type :switch :win-percent switch-wins}]))
                     (range 1 (inc experiments)))]
    {:data {:values data}
     :width width
     :height height
     :title (format "%d experiments, %d doors, %d removals" experiments doors removals)
     :encoding {:x {:field "num-experiments" :type "quantitative"}
                :y {:field "win-percent" :type "quantitative"}
                :color {:field "type" :type "nominal"}}
     :mark "line"}))
