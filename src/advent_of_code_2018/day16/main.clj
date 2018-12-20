(ns advent-of-code-2018.day16.main
  (:require [clojure.string :as cstr]
            [clojure.set :as cset]))

(defn rr-instr [f] (fn [regs [a b c]] (assoc regs c (f (regs a) (regs b)))))
(defn ri-instr [f] (fn [regs [a b c]] (assoc regs c (f (regs a) b))))
(defn ir-instr [f] (fn [regs [a b c]] (assoc regs c (f a (regs b)))))
(defn ii-instr [f] (fn [regs [a b c]] (assoc regs c (f a b))))

(def INSTRS {
  :addr (rr-instr +)
  :addi (ri-instr +)
  :mulr (rr-instr *)
  :muli (ri-instr *)
  :banr (rr-instr bit-and)
  :bani (ri-instr bit-and)
  :borr (rr-instr bit-or)
  :bori (ri-instr bit-or)
  :setr (rr-instr (fn [a b] a))
  :seti (ii-instr (fn [a b] a))
  :gtir (ir-instr (fn [a rb] (if (> a rb) 1 0)))
  :gtri (ri-instr (fn [ra b] (if (> ra b) 1 0)))
  :gtrr (rr-instr (fn [ra rb] (if (> ra rb) 1 0)))
  :eqir (ir-instr (fn [a rb] (if (= a rb) 1 0)))
  :eqri (ri-instr (fn [ra b] (if (= ra b) 1 0)))
  :eqrr (rr-instr (fn [ra rb] (if (= ra rb) 1 0)))
})

(defn instr-applies? [instr operands in-regs out-regs]
  (= (instr in-regs operands) out-regs))

(defn get-candidates [operands in-regs out-regs]
  (set (filter (fn [k] (instr-applies? (k INSTRS) operands in-regs out-regs))
          (keys INSTRS))))

(defn parse-pt1-input [filename]
  (let [matches (re-seq #"(?s)Before: \[(\d)(?:, )(\d)(?:, )(\d)(?:, )(\d)\].*?(\d+) (\d+) (\d+) (\d+).*?After:  \[(\d)(?:, )(\d)(?:, )(\d)(?:, )(\d)\]" (slurp filename))]
    (map (fn [[_ & ms]] (let [[a b c d e f g h i j k l] (map #(Integer/parseInt %) ms)]
                          ; instruction, in-regs, out-regs
                          [[e f g h] [a b c d] [i j k l]]))
         matches)))

(defn parse-program [filename]
  (let [matches (re-seq #"(\d+) (\d+) (\d+) (\d+)" (slurp filename))]
    (map (fn [[_ & ms]] (let [[opcode a b c] (map #(Integer/parseInt %) ms)]
                          [opcode a b c]))
         matches)))

(defn pt1 []
  (let [inputs (parse-pt1-input "src/advent_of_code_2018/day16/input.txt")
        counts (map (fn [[instr in-reg out-reg]] (count (get-candidates (rest instr) in-reg out-reg)))
               inputs)]
    (println (count (filter #(>= % 3) counts)))))

(defn find-instr-by-opcode [appliable-by-opcode instr-by-opcode seen-instrs]
  (if (= (count appliable-by-opcode) (count seen-instrs))
    instr-by-opcode
    (let [new-appliable-by-opcode (into {} (map (fn [[k v]] [k (cset/difference v seen-instrs)])
                                                appliable-by-opcode))
          [opcode instrs] (first (filter (fn [[k v]] (= (count v) 1)) new-appliable-by-opcode))]
      (recur new-appliable-by-opcode
             (assoc instr-by-opcode opcode (first instrs))
             (conj seen-instrs (first instrs))))))

(defn run-instr [[opcode a b c] regs instr-by-opcode]
  (((instr-by-opcode opcode) INSTRS) regs [a b c]))

(defn run-program [instrs regs instr-by-opcode]
  (if (empty? instrs)
    regs
    (recur (rest instrs)
           (run-instr (first instrs) regs instr-by-opcode)
           instr-by-opcode)))

(defn pt2 []
  (let [inputs (parse-pt1-input "src/advent_of_code_2018/day16/input.txt")
        program (parse-program "src/advent_of_code_2018/day16/input-program.txt")
        appliable-by-opcode (reduce
          (fn [acc [instr in-reg out-reg]]
            (let [[opcode & operands] instr
                  candidates (get-candidates operands in-reg out-reg)]
              (update acc opcode (fn [v] (if (nil? v)
                                             candidates
                                             (cset/intersection candidates v))))))
          {}
          inputs)
        instr-by-opcode (find-instr-by-opcode appliable-by-opcode {} #{})]
    ; Final registers state
    (println (run-program program [0 0 0 0] instr-by-opcode))))

(pt1)
(pt2)
