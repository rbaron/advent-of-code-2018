(ns advent-of-code-2018.day8.main
  (:require [clojure.string :as cstr]))

(defn read-input [filename]
  (->> (-> (slurp filename)
           cstr/trim
           (cstr/split #" "))
       (map #(Integer/parseInt %))
       vec))

(defn get-value [children metadata]
  (if (empty? children)
    (apply + metadata)
    ; Indices are 1-based, so we decrement them
    (let [indices (map dec metadata)
          nodes   (remove nil? (map #(get children %) indices))]
      (apply + (map :value nodes)))))

(defn parse-node [input pos]
  (let [n-children (get input pos)
        n-metadata (get input (inc pos))
        children   (reduce (fn [acc child-n]
                               (let [child-pos (+ 2 pos (apply + (map :size acc)))
                                     child (parse-node input child-pos)]
                                  (conj acc child)))
                            []
                            (range n-children))
        children-size (apply + (map :size children))
        metadata      (map input (range (+ 2 pos children-size)
                                        (+ 2 pos children-size n-metadata)))]
    {:acc-metadata (+ (apply + metadata) (apply + (map :acc-metadata children)))
     :metadata      metadata
     :value         (get-value children metadata)
     :children      children
     :size          (+ 2 n-metadata (apply + (map :size children)))}))

(defn run []
  (let [input (read-input "src/advent_of_code_2018/day8/input.txt")
        root  (parse-node input 0)]
    ; Part 1
    (println (:acc-metadata root))
    ; Part 2
    (println (:value root))))

(run)
