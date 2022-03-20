(ns monty.experiment-test
  (:require [monty.experiment :as sut]
            [clojure.test :as t]))



(t/deftest door-selections-test
  (doseq [_ (range 20)
          [n-doors n-removals] [[3 1]
                                [5 1]
                                [5 2]
                                [5 3]
                                [10 8]
                                [100 80]]]
    (let [{:keys [first-selection switched-selection host-selections]}
          (sut/door-selections n-doors n-removals)]
      (t/is (not (host-selections first-selection))
            "Host should not select a contestant's first selection")
      (t/is (not (host-selections switched-selection))
            "Contestant should not select host's selection as their second selection"))))



(t/deftest expected-probability-of-win
  (let [[no-switch-win-percent switch-win-percent] (sut/compute-win-percent 1000000 3 1)]
    (t/is (= 33.0 (Math/floor no-switch-win-percent)))
    (t/is (= 66.0 (Math/floor switch-win-percent)))))
