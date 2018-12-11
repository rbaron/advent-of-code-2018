(ns advent-of-code-2018.day9.main
  (:require [clojure.string :as cstr]))

(defn make-node [value prv nxt]
  (atom {:value value :prev prv :next nxt}))

(defn init-lst [value]
  (let [node (make-node value (atom nil) (atom nil))]
    (swap! node #(assoc % :prev node :next node))
    node))

(defn find-node [curr-node pos]
  (cond
    (= pos 0) curr-node
    (< pos 0) (recur (:prev @curr-node) (inc pos))
    :else     (recur (:next @curr-node) (dec pos))))

(defn insert! [value curr-node pos]
  (let [node     (find-node curr-node pos)
        new-node (make-node value node (:next @node))]
    (swap! (:next @node) (fn [n] (assoc n :prev new-node)))
    (swap! node          (fn [n] (assoc n :next new-node)))
    new-node))

(defn remove! [curr-node pos]
  (let [node                (find-node curr-node pos)
        {:keys [prev next]} @node]
    (swap! prev (fn [n] (assoc n :next next)))
    (swap! next (fn [n] (assoc n :prev prev)))
    node))

(defn print-lst
  ([node]
    (print-lst node (set [])))
  ([node visited]
    (if (contains? visited node)
      (println (:value @node))
      (do
        (print (:value @node) " -> ")
        (recur (:next @node) (conj visited node))))))

(defn play [curr-node moves scoreboard]
  (if (empty? moves)
    scoreboard
    (let [[elf marble] (first moves)]
      (if (= (rem marble 23) 0)
        (let [removed (remove! curr-node -7)]
          (recur (:next @removed)
                 (rest moves)
                 (update scoreboard elf (fnil #(+ % (:value @removed) marble) 0))))
        (let [new-current (insert! marble curr-node 1)]
          (recur new-current
                 (rest moves)
                 scoreboard))))))

(defn solve []
  (let [
        curr-node   (init-lst 0)
        ;n-players   9
        ;last-marble 25
        n-players   411
        last-marble (* 72059 100)
        moves       (map vector (cycle (range n-players)) (range 1 (inc last-marble)))
        scoreboard  (play curr-node moves {})]
    (apply max (vals scoreboard))))

(solve)
