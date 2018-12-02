(ns advent-of-code-2018.day1.main
  (:gen-class))

(defn read-input
  [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (map (fn [n] (Integer/parseInt n))
         (doall (line-seq rdr)))))

(defn -pt1
  [& args]
    (let [deltas (read-input "input.txt")]
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
