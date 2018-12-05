(ns advent-of-code-2018.day5.main
  (:require [clojure.string :as cstr]))

(defn polar-oposite?  [c1 c2]
  (and c1 c2 (not= c1 c2) (= (cstr/lower-case c1) (cstr/lower-case c2))))

(defn collapse-ignoring
  [stack remaining ignore-char]
  (cond
    (empty? remaining)
      (count stack)
    (= (first (cstr/lower-case (first remaining))) ignore-char)
      (recur stack (rest remaining) ignore-char)
    (polar-oposite? (peek stack) (first remaining))
      (recur (pop stack) (rest remaining) ignore-char)
    :else
      (recur (conj stack (first remaining)) (rest remaining) ignore-char)))

(defn pt1 []
  (let [polymer (cstr/trim-newline (slurp "src/advent_of_code_2018/day5/input.txt"))]
    (println (collapse-ignoring [] polymer nil))))

(defn pt2 []
  (let [polymer (cstr/trim-newline (slurp "src/advent_of_code_2018/day5/input.txt"))
        units   (map char (range (int \a) (+ (int \z) 1)))
        winner  (apply min-key #(collapse-ignoring [] polymer %) units)]
    (println (collapse-ignoring [] polymer winner))))
