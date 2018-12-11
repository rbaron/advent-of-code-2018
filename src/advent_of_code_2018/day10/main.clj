(ns advent-of-code-2018.day10.main
  (:require [clojure.string :as cstr]))

(defn read-input [filename]
  (->>(cstr/split-lines (slurp filename))
      (map (fn [s] (let [matches (into [] (re-seq #"(\-?\d+)" s))
                         [x y vx vy] (map #(Integer/parseInt (second %)) matches)]
                     {:x x :y y :vx vx :vy vy})))))

(defn bounds [points]
  (let [max-x (apply max (map :x points))
        min-x (apply min (map :x points))
        max-y (apply max (map :y points))
        min-y (apply min (map :y points))]
    {:min-x min-x :max-x max-x :min-y min-y :max-y max-y}))

(defn plot-points [points {:keys [max-x min-x max-y min-y]}]
  (let [coords (set (map (juxt :x :y) points))]
     (doseq [y (range min-y (inc max-y))
             x (range min-x (inc max-x))]
        (print (if (contains? coords [x y]) "#" " "))
        (when (= x max-x) (println)))
     (println)))

(defn update-points [points]
  (map (fn [{:keys [x y vx vy]}] {
          :x (+ x vx) :y (+ y vy) :vx vx :vy vy
        })
        points))

(defn area [{:keys [min-x max-x min-y max-y]}]
  (* (Math/abs (- min-x max-x)) (Math/abs (- min-y max-y))))

(defn history [points iteration]
    (lazy-seq (cons [points iteration] (history (update-points points) (inc iteration)))))

; Pt 1 and 2
(let [points     (read-input "src/advent_of_code_2018/day10/input.txt")
      hist       (history points 0)
      [msg iter] (apply min-key (fn [[pts iter]] (area (bounds pts))) (take 12000 hist))]
       (plot-points msg (bounds msg))
       (println "Iteration #" iter))
