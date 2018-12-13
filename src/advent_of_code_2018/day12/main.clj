(ns advent-of-code-2018.day12.main
  (:require [clojure.string :as cstr]))

(defn read-input [filename]
  (let [[header _ & rules] (cstr/split-lines (slurp filename))
         state             (last (cstr/split header #" "))]
    {:state (into {} (map-indexed (fn [i s] [i s]) state))
     :rules (map (fn [r] (let [[rule-from rule-to] (cstr/split r #" => ")]
                           [(into [] rule-from) (first rule-to)]))
                 rules)}))

(defn rule-applies? [state i rule]
  (let [[rule-from rule-to] rule
        local-state (map (fn [j] (get state j \.)) (range (- i 2) (+ i 3)))]
    (= local-state rule-from)))

(defn apply-rule [state i rule]
  (let [[rule-from rule-to] rule]
      (assoc state i rule-to)))

(defn apply-rules [state i rules]
  (if (empty? rules)
    \.
    (if (rule-applies? state i (first rules))
      (get (apply-rule state i (first rules)) i)
      (recur state i (rest rules)))))

(defn evolve [state rules]
  (let [min-index (- (apply min (keys state)) 2)
        max-index (+ (apply max (keys state)) 2)]
    (into {} (map (fn [i] [i (apply-rules state i rules)])
                  (range min-index (inc max-index))))))

(defn print-state [state]
    (println (cstr/join (map (fn [i] (get state i \.)) (range -3 35)))))

(defn evolve-forever [state rules i n-plants]
  (let [new-state (evolve state rules)
        new-n-plants (reduce (fn [c [k v]] (if (= v \#) (+ c k) c)) 0 new-state)]
      (println (inc i) new-n-plants (- new-n-plants n-plants))
      (evolve-forever new-state rules (inc i) new-n-plants)))

(defn pt1 []
  (let [{:keys [state rules]} (read-input "src/advent_of_code_2018/day12/input.txt")
        final-state (reduce (fn [s i]
                              (let [new-state (evolve s rules)]
                                ;(print-state s)
                                new-state))
                            state
                            (range 20))]
    ; pt1
    (println (reduce (fn [c [k v]] (if (= v \#) (+ c k) c)) 0 final-state))))

(defn pt2 []
  (let [{:keys [state rules]} (read-input "src/advent_of_code_2018/day12/input.txt")]
    (evolve-forever state rules 0 0)))

(pt1)

;(pt2)

; Running pt2 will print a constant pattern for generation (i) >= 111

;184 16965 87
;185 17052 87
;186 17139 87
;187 17226 87
;188 17313 87
;189 17400 87

; So we can guess what will happen on generation 50000000000:
(println (+ 16965 (* 87 (- 50000000000 184 ))))
