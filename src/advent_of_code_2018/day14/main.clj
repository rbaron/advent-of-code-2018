(ns advent-of-code-2018.day14.main
  (:require [clojure.string :as cstr]))

(defn step [recipes elf1i elf2i]
  (let [new-recipe  (apply + (map recipes [elf1i elf2i]))
        digits      (map #(Integer/parseInt %) (cstr/split (.toString new-recipe) #""))
        new-recipes (into recipes digits)
        new-elf1i   (mod (+ elf1i 1 (recipes elf1i)) (count new-recipes))
        new-elf2i   (mod (+ elf2i 1 (recipes elf2i)) (count new-recipes))]
    [new-recipes new-elf1i new-elf2i]))

(defn pt1
  []
  (let [recipes [3 7]
        elf1i   0
        elf2i   1
        size    10
        times   702831]
    (let [[final-recipes & rst] (reduce (fn [[rs i1 i2] iter]
                                          ;(when (= 0 (mod iter 1000)) (println `iter iter))
                                          (step rs i1 i2))
                                  [recipes elf1i elf2i]
                                  (range (+ times size)))]
      (println (cstr/join (take size (drop times final-recipes)))))))

(pt1)

(defn search-score [recipes start-idx score]
  (cond
    (empty? score)
      true
    (= (get recipes start-idx) (first score))
      (recur recipes (inc start-idx) (rest score))
    :else
      false))

(defn run-until-score-is-found
  [recipes elf1i elf2i score]
  (let [[new-rec i1 i2] (step recipes elf1i elf2i)
        start-idx       (- (count new-rec) (count score))]
    ;(when (= 0 (mod start-idx 1000)) (println 'iter start-idx))
    (cond (search-score new-rec start-idx score) start-idx
          (search-score new-rec (dec start-idx) score) (dec start-idx)
          :else (recur new-rec i1 i2 score))))

(defn pt2
  []
  (let [recipes [3 7]
        elf1i   0
        elf2i   1
        score   [7 0 2 8 3 1]]
    (println (run-until-score-is-found recipes elf1i elf2i score))))

(pt2)
