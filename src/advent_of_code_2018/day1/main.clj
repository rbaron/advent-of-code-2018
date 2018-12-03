(ns advent-of-code-2018.day1.main
  (:require [clojure.string :as cstr])
  (:gen-class))

(defn read-input
  [filename]
  (map #(Integer/parseInt %) (cstr/split-lines (slurp filename))))

(defn -pt1
  [& args]
    (let [deltas (read-input "src/advent_of_code_2018/day1/input.txt")]
      (println (reduce + deltas))))

(defn find-1st-repeated-state
  ([collection state         ] (find-1st-repeated-state collection state 0 #{}))
  ([collection state idx seen]
    (if (contains? seen state)
      state
      (recur collection
             (+ state (get collection idx))
             (mod (+ idx 1) (count collection))
             (conj seen state)))))

(defn -pt2
  [& args]
    (let [deltas (read-input "src/advent_of_code_2018/day1/input.txt")]
      (println "Repeated: " (find-1st-repeated-state (vec deltas) 0))))
