(ns advent-of-code-2018.day11.main
  (:require [clojure.string :as cstr]))

(defn get-power-level [x y serial]
    (let [rack-id (+ x 10)
         p1      (+ serial (* rack-id y))
         p2      (* rack-id p1)
         p3      (quot (rem p2 1000) 100)]
      (- p3 5)))

(def grid-power
  (memoize (fn [x y size serial]
    (if (= size 1)
      (get-power-level x y serial)
      (let [subgrid-power (grid-power x y (dec size) serial)
            new-col-x     (dec (+ x size))
            new-row-y     (dec (+ y size))
            new-col       (map (fn [row] [new-col-x row]) (range y (+ y (dec size))))
            new-row       (map (fn [col] [col new-row-y]) (range x (+ x (dec size))))
            new-coords    (conj (concat new-col new-row) [new-col-x new-row-y])]
         (+ subgrid-power (apply + (map (fn [[x y]] (get-power-level x y serial)) new-coords))))))))

(defn gen-grids []
  (for [size (range 1 301)
        x   (range 1 (- 301 size))
        y   (range 1 (- 301 size))]
     [x y size]))

(defn pt1 []
  (let [serial 6548
        grids (gen-grids)
        [x y size] (apply max-key (fn [[x y size]] (grid-power x y size serial)) grids)]
    (println x y size (grid-power x y size serial))))

(pt1)
