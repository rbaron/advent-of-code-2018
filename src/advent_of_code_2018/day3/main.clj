(ns advent-of-code-2018.day3.main
  (:require [clojure.string :as cstr])
  (:gen-class))

(defn read-input
  [filename]
  (cstr/split-lines (slurp filename)))

(defn parse-patch
  [string]
  (let [matches (re-matches #"#(\d+) @ (\d+),(\d+): (\d+)x(\d+)" string)
        [id x y w h] (map #(Integer/parseInt %) (rest matches))]
    {:id id
     :x x
     :y y
     :w w
     :h h}))

(defn gen-cells
  [{:keys [x y w h]}]
  (for [i (range x (+ x w))
        j (range y (+ y h))]
    [i j]))

(defn update-cells
  [cells patch]
  (reduce (fn [cells cell] (assoc cells cell (inc (get cells cell 0))))
          cells
          (gen-cells patch)))

(defn -pt1
  [& args]
  (let [lines (read-input "src/advent_of_code_2018/day3/input.txt")
        patches (map parse-patch lines)
        cells (reduce update-cells {} patches)]
    (count (filter #(>= % 2) (vals cells)))))

(defn did-not-overlap
  [final-cells patch]
  (every? #(= (get final-cells %) 1) (gen-cells patch)))

(defn -pt2
  [& args]
  (let [lines (read-input "src/advent_of_code_2018/day3/input.txt")
        patches (map parse-patch lines)
        final-cells (reduce update-cells {} patches)]
      (:id (first (filter #(did-not-overlap final-cells %) patches)))))
