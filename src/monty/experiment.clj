(ns monty.experiment)

(defn door-selections
  [n-doors n-removals]
  {:pre [(< n-removals (dec n-doors))]}
  (let [all-doors (range n-doors)
        winning-door (rand-int n-doors)
        contestant-first-selection (rand-int n-doors)
        host-selections (->> all-doors
                             shuffle
                             (remove (set [winning-door contestant-first-selection]))
                             (take n-removals)
                             set)
        chosen-already? (conj host-selections contestant-first-selection)
        contestant-switched-selection (first (remove chosen-already? (shuffle all-doors)))]
    {:first-selection contestant-first-selection
     :winning-door winning-door
     :host-selections host-selections
     :switched-selection contestant-switched-selection}))


(defn run-experiment
  [n-doors n-removals]
  (let [{:keys [first-selection switched-selection winning-door]}
        (door-selections n-doors n-removals)]
    [(if (= winning-door first-selection) 1 0)
     (if (= winning-door switched-selection) 1 0)]))


(defn run-repeated-experiment
  [n-experiments n-doors n-removals]
  (let [[first-selection-wins
         second-selection-wins] (reduce (fn [[prev-a prev-b] [a b]]
                                          [(+ a prev-a) (+ b prev-b)])
                                        [0 0]
                                        (repeatedly n-experiments #(run-experiment n-doors n-removals)))] 
    [(* 100 (double (/ first-selection-wins n-experiments)))
     (* 100 (double (/ second-selection-wins n-experiments)))]))


(defn monte-carlo
  [n-experiments n-doors n-removals & {:keys [width height]
                                       :or {width 200 height 200}}]
  (let [data (mapcat (fn [num-experiment]
                       (let [[no-switch-wins switch-wins] (run-repeated-experiment num-experiment n-doors n-removals)]
                         [{:num-experiments num-experiment :type :no-switch :win-percent no-switch-wins}
                          {:num-experiments num-experiment :type :switch :win-percent switch-wins}]))
                     (range 1 (inc n-experiments)))]
    {:data {:values data}
     :width width
     :height height
     :title (format "%d experiments, %d doors, %d removals" n-experiments n-doors n-removals)
     :encoding {:x {:field "num-experiments" :type "quantitative"}
                :y {:field "win-percent" :type "quantitative"}
                :color {:field "type" :type "nominal"}}
     :mark "line"}))
