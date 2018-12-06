(ns advent-of-code-2018.day6.main
  (:require [clojure.string :as cstr]))

(defn read-input
  [filename]
  (let [lines (cstr/split-lines (slurp filename))
        coords (map #(cstr/split % #", ") lines)
        to-ints (fn [coord] (map #(Integer/parseInt %) coord))]
    (map to-ints coords)))

(defn get-bounds
  [coords]
  (let [xs (map first coords)
        ys (map second coords)]
    {:min-x (apply min xs) :max-x (apply max xs)
     :min-y (apply min ys) :max-y (apply max ys)}))

(defn is-boundary?
  [[x y] bounds]
  (or (= x (:min-x bounds)) (= x (:max-x bounds))
      (= y (:min-y bounds)) (= y (:max-y bounds))))

(defn manhattan
  [[x1 y1] [x2 y2]]
  (+ (Math/abs (- x1 x2)) (Math/abs (- y1 y2))))

(defn gen-grid-coords
  [bounds]
  (for [x (range (:min-x bounds) (inc (:max-x bounds)))
         y (range (:min-y bounds) (inc (:max-y bounds)))]
    [x y]))

(defn update-grid-coord
  [grid grid-coord coord]
  (if-let [current-value (get grid grid-coord {})]
    (let [{:keys [winner-coord dist] :or {dist Double/POSITIVE_INFINITY}} current-value
          new-dist (manhattan coord grid-coord)]
      (if (< new-dist dist)
        (assoc grid grid-coord {:winner-coord coord :dist new-dist})
        grid))
  grid))

(defn update-grid
  [grid coord grid-coords]
  (reduce (fn [acc grid-coord] (update-grid-coord acc grid-coord coord))
          grid
          grid-coords))

(defn get-area-around-coord
  "Returns -inf for infinite areas"
  [cells-by-coord bounds coord]
  (if (some #(is-boundary? % bounds) (get cells-by-coord coord))
    Double/NEGATIVE_INFINITY
    (count (get cells-by-coord coord))))

(defn pt1 []
  (let [coords (read-input "src/advent_of_code_2018/day6/input.txt")
        bounds (get-bounds coords)
        grid-coords (gen-grid-coords bounds)
        grid        (reduce (fn [acc coord] (update-grid acc coord grid-coords)) {} coords)
        cells-by-coord (group-by (fn [grid-coord] (:winner-coord (get grid grid-coord))) grid-coords)
        areas       (map #(get-area-around-coord cells-by-coord bounds %) coords)]
    (println (apply max areas))))

(defn update-grid-coord2
  [grid grid-coord coord]
  (let [current-value (get grid grid-coord 0)
        new-dist (manhattan coord grid-coord)]
      (assoc grid grid-coord (+ current-value new-dist))))

(defn update-grid2
  [grid coord grid-coords]
  (reduce (fn [acc grid-coord] (update-grid-coord2 acc grid-coord coord))
          grid
          grid-coords))

(defn pt2 []
  (let [coords (read-input "src/advent_of_code_2018/day6/input.txt")
        bounds (get-bounds coords)
        grid-coords (gen-grid-coords bounds)
        grid        (reduce (fn [acc coord] (update-grid2 acc coord grid-coords)) {} coords)]
    (println (count (filter #(< % 10000) (vals grid))))))
