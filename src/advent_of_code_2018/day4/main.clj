(ns advent-of-code-2018.day4.main
  (:require [clojure.string :as cstr])
  (:gen-class))

(defn read-input
  [filename]
  (cstr/split-lines (slurp filename)))

; Will sorting lexicographically suffice? Seems like it.
(def sort-input sort)

(defn get-minute
  [line]
  (let [[_ minute] (re-find #"00:(\d+)" line)]
    (Integer/parseInt minute)))

(defn get-id
  [line]
  (let [[_ id] (re-find #"Guard #(\d+)" line)]
    (Integer/parseInt id)))

(defn get-sleep-intervals-by-id
  ([records]
    (get-sleep-intervals-by-id records {} nil nil))
  ([records sleep-intervals-by-id current-id slept-at-minute]
    (if-let [record (first records)]
      (cond
        (cstr/includes? record "falls asleep")
          (recur (rest records) sleep-intervals-by-id current-id (get-minute record))
        (cstr/includes? record "wakes up")
          (let [interval [slept-at-minute (get-minute record)]
                new-intervals (conj (get sleep-intervals-by-id current-id []) interval)]
            (recur (rest records)
                   (assoc sleep-intervals-by-id current-id new-intervals)
                   current-id
                   nil))
        (cstr/includes? record "Guard")
          (recur (rest records) sleep-intervals-by-id (get-id record) nil))
      sleep-intervals-by-id)))

(defn get-total-time
  [intervals]
  (apply + (map #(- (second %) (first %)) intervals)))

(defn get-most-slept-minute
  [intervals days-by-minute]
  (if-let [interval (first intervals)]
    (recur (rest intervals)
           (reduce (fn [acc minute] (update acc minute (fnil inc 0)))
                   days-by-minute
                   (apply range interval)))
    (first (apply max-key second days-by-minute))))

(defn pt1
  [& args]
  (let [records (sort-input (read-input "src/advent_of_code_2018/day4/input.txt"))
        intervals-by-id (get-sleep-intervals-by-id records)
        [id intervals] (apply max-key #(get-total-time (second %)) intervals-by-id)
        most-slept-min (get-most-slept-minute intervals {})]
    (println id most-slept-min)
    (println (* id most-slept-min))))

(defn make-histogram
  [intervals]
  (let [sub-histograms (map #(into {} (map vector (apply range %) (repeat 1))) intervals)]
    (apply merge-with + sub-histograms)))

(defn make-histogram-by-id
  [intervals-by-id]
  (into {} (map (fn [[k v]] [k (make-histogram v)]) intervals-by-id)))

(defn get-max-val
  [[_ histogram]]
  (apply max (vals histogram)))

(defn pt2
  [& args]
  (let [records (sort-input (read-input "src/advent_of_code_2018/day4/input.txt"))
        intervals-by-id  (get-sleep-intervals-by-id records)
        histograms-by-id (make-histogram-by-id intervals-by-id)
        [id histogram] (apply max-key get-max-val histograms-by-id)
        [min count]    (apply max-key second histogram)]
    (println (* id min))))
