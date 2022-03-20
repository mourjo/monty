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





