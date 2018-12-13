(ns advent-of-code-2018.day13.main
  (:require [clojure.string :as cstr]))

(defn read-input [filename]
  (into [] (->> (cstr/split-lines (slurp filename))
        (map #(cstr/split % #"")))))

(defn find-cars [grid]
  (defn gen-contents []
    (for [y (range (count grid))
          x (range (count (first grid)))]
      [[y x] (get-in grid [y x])]))

    (def vehicle-strings (set (cstr/split "<>v^" #"")))

    (map-indexed (fn [i [[y x] c]] [i y x c 0])
         (filter (fn [[[y x] c]] (contains? vehicle-strings c))
                 (gen-contents))))

(defn step-car [grid [i y x c turn-count]]
  (def increment {
    ">" [ 0  1]
    "<" [ 0 -1]
    "^" [-1  0]
    "v" [ 1  0]
  })

  (def turn-left {
    ">" "^"
    "^" "<"
    "<" "v"
    "v" ">"
  })

  (def turn-right {
    ">" "v"
    "v" "<"
    "<" "^"
    "^" ">"
  })

  (defn rotate [c turn-count]
    (cond (= (rem turn-count 3) 0) (turn-left c)
          (= (rem turn-count 3) 1) c
          (= (rem turn-count 3) 2) (turn-right c)))

  (let [[ny nx]    (vec (map + (increment c) [y x]))
        curr-place (get-in grid [ny nx])]
    (cond (= curr-place "+") [i ny nx (rotate c turn-count) (inc turn-count)]
          (= curr-place "\\") (cond (= c ">") [i ny nx "v" turn-count]
                                    (= c "^") [i ny nx "<" turn-count]
                                    (= c "<") [i ny nx "^" turn-count]
                                    (= c "v") [i ny nx ">" turn-count])
          (= curr-place "/")  (cond (= c ">") [i ny nx "^" turn-count]
                                    (= c "v") [i ny nx "<" turn-count]
                                    (= c "^") [i ny nx ">" turn-count]
                                    (= c "<") [i ny nx "v" turn-count])
          :else [i ny nx c turn-count])))

(defn collides? [[i y x & rst] cars]
  (some (fn [[j cy cx & rst :as car]] (and (= y cy) (= x cx) car)) cars))

(defn step-cars [grid cars iter]
  (let [cars-by-id (into {} (map (fn [[i y x c turn-count :as car]] [i car]) cars))
        new-positions-by-car (reduce
          (fn [acc car]
            (let [[i ny nx c turn-count :as new-car] (step-car grid car)]
              (when (collides? new-car (vals acc))
                (println "Collision!" new-car iter))
              (assoc acc i new-car)))
          cars-by-id
          cars)]

  (sort-by (fn [[i y x & rst]] [y x]) (vals new-positions-by-car))))

(defn pt1 []
  (let [grid (read-input "src/advent_of_code_2018/day13/input.txt")
        cars (find-cars grid)
        final-cars (reduce (fn [cars i] (step-cars grid cars i))
                           cars
                           (range 1000))]
    (println 'done)))

(defn step-cars-pt2 [grid cars iter]
  (def collided (atom (set [])))

  (let [cars-by-id (into {} (map (fn [[i y x c turn-count :as car]] [i car]) cars))
        new-positions-by-car (reduce
          (fn [acc car]
            (let [[i ny nx c turn-count :as new-car] (step-car grid car)
                  collided-with (collides? new-car (vals acc))]
              (when collided-with
                (swap! collided #(conj % (collided-with 0)))
                (swap! collided #(conj % (new-car 0)))
                (println "Collision!" new-car iter))
              (assoc acc i new-car)))
          cars-by-id
          cars)]

  (let [remaining-cars (remove #(contains? @collided (% 0)) (vals new-positions-by-car))]
    (if (= (count remaining-cars) 1)
      (println "Only one car remaining" (first remaining-cars))
      (sort-by (fn [[i y x & rst]] [y x]) remaining-cars)))))

(defn pt2 []
  (let [grid (read-input "src/advent_of_code_2018/day13/input.txt")
        cars (find-cars grid)
        final-cars (reduce (fn [cars i] (step-cars-pt2 grid cars i))
                           cars
                           (range 100000))]
    (println 'done)))

(println "Part 1")
(pt1)

(println "Part 2")
(pt2)
