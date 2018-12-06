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

(defn grid-cell-coord
  [cell coords]
  (let [[d1 d2 & rst] (sort (map #(manhattan cell %) coords))]
    (if (= d1 d2)
      Double/NEGATIVE_INFINITY
      (apply min-key #(manhattan cell %) coords))))

(defn get-area-around-coord
  [cells-by-coord bounds coord]
  (if (some #(is-boundary? % bounds) (get cells-by-coord coord))
    Double/NEGATIVE_INFINITY
    (count (get cells-by-coord coord))))

(defn pt1
  []
  (let [coords         (read-input "src/advent_of_code_2018/day6/input.txt")
        bounds         (get-bounds coords)
        cells-by-coord (group-by (fn [cell] (grid-cell-coord cell coords)) (gen-grid-coords bounds))
        areas          (map #(get-area-around-coord cells-by-coord bounds %) coords)]
    (println (apply max areas))))

(defn sum-of-dists
  [cell coords]
  (apply + (map #(manhattan cell %) coords)))

(defn pt2
  []
  (let [coords (read-input "src/advent_of_code_2018/day6/input.txt")
        bounds (get-bounds coords)
        sums   (map #(sum-of-dists % coords) (gen-grid-coords (get-bounds coords)))]
    (println (count (filter #(< % 10000) sums)))))
