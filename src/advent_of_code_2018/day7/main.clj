(ns advent-of-code-2018.day7.main
  (:require [clojure.string :as cstr]))

(defn parse-line [line]
  (let [[_ req task] (re-find #"Step (\w) .* before step (\w)" line)]
    [task req]))

(defn read-input [filename]
  (->> (slurp filename)
       cstr/split-lines
       (map parse-line)))

(defn feasible? [reqs-by-task task finished]
  (if-let [reqs (get reqs-by-task task)]
    (every? (fn [req] (some #(= req %) finished)) reqs)
    true))

(defn solve-in-order [reqs-by-task queue finished]
  (if (empty? queue)
    finished
    (let [task (first (filter #(feasible? reqs-by-task % finished) queue))]
      (recur reqs-by-task (filter #(not= % task) queue) (conj finished task)))))

(defn get-reqs-by-task [nodes]
    (reduce (fn [acc [task req]]
            (assoc acc task (conj (get acc task []) req)))
            {}
            nodes))

(defn pt1 []
  (let [nodes (read-input "src/advent_of_code_2018/day7/input.txt")
        reqs-by-task (get-reqs-by-task nodes)
        queue (sort (set (reduce (fn [acc [t r]] (concat acc [t r])) () nodes)))]
    (println (cstr/join (solve-in-order2 reqs-by-task queue [])))))

(def n-slots 5)

(def runtime-offset 60)

(defn is-finished? [{:keys [task runtime]}]
  (>= runtime (- (+ runtime-offset (int (first task))) (int \A))))

(defn solve-in-parallel [reqs-by-task in-progress queue finished runtime]
  (if (and (empty? queue) (empty? in-progress))
    runtime
    (let [finished-tasks    (map :task (filter is-finished? in-progress))
          still-in-progress (filter #(not (is-finished? %)) in-progress)
          new-finished      (concat finished finished-tasks)
          available-tasks   (filter #(feasible? reqs-by-task % new-finished) queue)
          available-slots   (- n-slots (count still-in-progress))
          taken-tasks       (take available-slots available-tasks)
          new-progress      (map (fn [t] {:task t :runtime -1})
                                  (take available-slots available-tasks))
          updated-progress  (map (fn [prog] {:task (:task prog) :runtime (inc (:runtime prog))})
                                 (concat still-in-progress new-progress))
          new-queue         (filter #(not (or ((set finished-tasks) %) ((set taken-tasks) %)))  queue)]
      (recur reqs-by-task updated-progress new-queue new-finished (inc runtime)))))

(defn pt2 []
  (let [nodes (read-input "src/advent_of_code_2018/day7/input.txt")
        reqs-by-task (get-reqs-by-task nodes)
        queue (sort (set (reduce (fn [acc [t r]] (concat acc [t r])) () nodes)))]
    (println (solve-in-parallel reqs-by-task [] queue [] -1))))

(pt1)

(pt2)
