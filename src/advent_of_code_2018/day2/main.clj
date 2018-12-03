(ns advent-of-code-2018.day2.main
  (:require [clojure.string :as cstr])
  (:gen-class))

(defn read-input
  [filename]
  (cstr/split-lines (slurp filename)))

(defn make-count
  [string]
  (reduce (fn [count el]
            (assoc count el (inc (get count el 0))))
          {}
          (seq string)))

(defn has-count
  [count n]
  (if (some (fn [v] (= v n)) (vals count))
    1
    0))

(defn -pt1
  [& args]
  (let [words (read-input "src/advent_of_code_2018/day2/input.txt")
        counts (map make-count words)]
    (+ (apply + (map #(has-count % 2) counts))
       (apply + (map #(has-count % 3) counts)))))

(defn distance
  [w1 w2]
  (apply + (map
        (fn [c1 c2] (if (= c1 c2) 0 1))
        (seq w1)
        (seq w2))))

(defn pairwise-combinations
  [coll]
  (for [x coll
        y coll]
    (vector x y)))

(defn -pt2
  [& args]
  (let [words (read-input "src/advent_of_code_2018/day2/input.txt")
        combs (pairwise-combinations words)
        [w1 w2] (first (filter (fn [[w1 w2]] (= (distance w1 w2) 1)) combs))]
    (printf "%s\n%s\n" w1 w2)))

