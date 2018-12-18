(ns advent-of-code-2018.day15.main
  (:require [clojure.string :as cstr]))

(defn read-input [filename]
  (let [lines (cstr/split-lines (slurp filename))]
    (vec (map (fn [line]
            (vec (map (fn [c]
              (cond (= \. c) {:type :terrain}
                    (= \# c) {:type :wall}
                    (= \E c) {:type :elf :hp 200}
                    (= \G c) {:type :goblin :hp 200}))
                  line)))
         lines))))

(defn get-neighbors [y x grid of-type]
  (filter (fn [[_ _ unit]] (= (:type  unit) of-type))
    (map (fn [[y x]] [y x (get-in grid [y x])]) [[(dec y) x] [y (dec x)] [y (inc x)] [(inc y) x]])))

(defn get-units [grid & types]
  (vec (filter (fn [[y x unit]] (some #(= % (:type unit)) types))
    (map (fn [[y x]] [y x (get-in grid [y x])])
         (for [y (range (count grid))
               x (range (count (first grid)))] [y x])))))


(def bfs (memoize (fn  [origin goal grid]

  (defn search [queue seen]
    (if (empty? queue)
      (do #_(println 'empty) Double/POSITIVE_INFINITY)
      (let [[[y x :as node] dist] (first queue)]
        ;(println 'search2queue (into [] queue))
        ;(println 'search2node node)
        ;(println 'search2seen seen)
        (if (contains? seen node)
          (recur (pop queue) seen)
          (if (= node goal)
            (do #_(println 'found) dist)
            (let [neighbors (get-neighbors y x grid :terrain)
                  new-nodes (map (fn [[ny nx _]] [[ny nx] (inc dist)])
                                 neighbors)]
              (recur (into (pop queue) new-nodes)
                     (into seen (conj new-nodes node)))))))))

  (let [queue (conj clojure.lang.PersistentQueue/EMPTY [origin 0])]
    (search queue (set [])))
)))

(def a* (memoize (fn  [[y0 x0] [y1 x1] grid]
  (def f-score (atom {}))
  (def g-score (atom {
    [y0 x0 (get-in grid [y0 x0])] 0
  }))
  (def came-from (atom {}))
  (def open (atom (set [[y0 x0 (get-in grid [y0 x0])]])))
  (def closed (atom (set [])))

  (defn dist [[a b] [c d]]
    (Math/sqrt (+ (Math/pow (- a c) 2) (Math/pow (- b d) 2))))

  (defn reconstruct-path [[y x unit :as goal]]
    (if (= [y x] [y0 x0])
      [goal]
      (if-let [c (@came-from goal)]
        (lazy-seq (cons goal (reconstruct-path c)))
        [goal])))

  (defn run []
    (if (empty? @open)
      nil
      (let [[y x unit :as current] (apply min-key #(get @f-score % Double/POSITIVE_INFINITY) @open)]
        ;(print-grid grid (reconstruct-path current) [y0 x0] [y1 x1])
        (if (= [y x] [y1 x1])
          (reconstruct-path current)
          (do
            (swap! open (fn [s] (remove #(= % current) s)))
            (swap! closed (fn [s] (conj s current)))
            (doseq [[ny nx _ :as neighbor] (get-neighbors y x grid :terrain)]
              (when (not (contains? @closed neighbor))
                (let [tent-g (inc (@g-score current))]
                  (swap! open (fn [s] (conj s neighbor)))
                  (when (<= tent-g (get @g-score neighbor Double/POSITIVE_INFINITY))
                    (swap! came-from #(assoc % neighbor current))
                    (swap! g-score   #(assoc % neighbor tent-g))
                    (swap! f-score   #(assoc % neighbor (+ tent-g (dist [ny nx] [y1 x1]))))))))
            ;(Thread/sleep 50)
            (recur))))))
  (run))))

(defn path-length [path]
  (if (nil? path)
    Double/POSITIVE_INFINITY
    (count path)))

; Switch between BFS and A* shortest path implementation implementation
;(defn afs [& args] (path-length (apply a* args)))
(def afs bfs)

(defn step-unit [[y x unit] grid elf-damage]
  (def enemy-type (if (= :elf (:type unit)) :goblin :elf))

  (defn maybe-attack [y x grid]
    (let [enemies-in-range (get-neighbors y x grid enemy-type)]
      (if (> (count enemies-in-range) 0)
        (let [[ey ex enemy]    (first (sort-by (fn [[ey ex e]] [(:hp e) ey ex]) enemies-in-range))
              damage (if (= (:type unit) :elf) elf-damage 3)
              new-hp (- (:hp enemy) damage)]
          (println "[attack] Unit" (:type unit) "at" y x "will attack" ey ex new-hp)
          (if (<= new-hp 0)
            (do #_(println "[remove] Will remove" ey ex) (assoc-in grid [ey ex] {:type :terrain}))
            (update-in grid [ey ex] (fn [u] (assoc u :hp new-hp)))))
        grid)))

  (defn move []
    (let [enemy-type (if (= :elf (:type unit)) :goblin :elf)
          enemies  (get-units grid enemy-type)
          in-range (mapcat (fn [[y x unit]] (get-neighbors y x grid :terrain)) enemies)
          sorted-in-range (sort-by (fn [[ny nx u]] [ny nx]) in-range)
          ]
      (if (empty? sorted-in-range)
        [y x grid]
        ;(let [path (apply min-key (fn [[ey ex enemy]] (path-length (a* [y x] [ey ex] grid))) (reverse sorted-in-range))
        ;(let [path (apply min-key (fn [[ey ex enemy]] (bfs [y x] [ey ex] grid)) (reverse sorted-in-range))
        (let [path (apply min-key (fn [[ey ex enemy]] (afs [y x] [ey ex] grid)) (reverse sorted-in-range))
          neighbors  (get-neighbors y x grid :terrain)]
          (if (or (nil? path) (empty? neighbors))
            [y x grid]
            (let [[ny nx _] path
                  [dy dx _] (apply min-key
                    ;(fn [[my mx _]] (path-length (a* [ny nx] [my mx] grid)))
                    ;(fn [[my mx _]] (bfs [ny nx] [my mx] grid))
                    (fn [[my mx _]] (afs [ny nx] [my mx] grid))
                    (reverse neighbors))]
              ;(if (not= Double/POSITIVE_INFINITY (bfs [dy dx] [ny nx] grid))
              (if (not= Double/POSITIVE_INFINITY (afs [dy dx] [ny nx] grid))
                (do (println "[move] Unit" (:type unit) "will move to" dy dx "from" y x)
                    [dy dx (assoc-in (assoc-in grid [dy dx] unit)
                                     [y x]
                                     {:type :terrain})])
                    [y x grid])))))))

  (let [new-grid (maybe-attack y x grid)]
    (if (= grid new-grid)
      (let [[ny nx new-grid2] (move)]
        (maybe-attack ny nx new-grid2))
      new-grid))
)

(defn step [grid elf-damage]
  (let [units (get-units grid :goblin :elf)]
    (reduce (fn [new-grid [y x unit]]
              ; Unit might have been killed earlier in this round
              #_(println 'here)
              (let [real-unit (get-in new-grid [y x])]
                (if (= (:type real-unit) :terrain)
                  new-grid
                  (step-unit [y x unit] new-grid elf-damage))))
            grid
            units)))


(defn print-grid [grid]
  (let [lines     (map (fn [line] (cstr/join "" (map (fn [{t :type}] (cond (= t :wall) \#
                                                         (= t :goblin) \G
                                                         (= t :elf) \E
                                                         (= t :terrain) \.
                                                         (= t :goal) "\033[32;41mX\033[0m"
                                                         (= t :origin) "\033[32;41mX\033[0m"
                                                         (= t :path) "\033[31mX\033[0m"))
                                                      line)))

                        grid)]
                  (println)
                  (println (cstr/join "\n" lines))))

(defn simulate [grid iter elf-damage]
  (if (not (and (> (count (get-units grid :goblin)) 0)
                (> (count (get-units grid :elf)) 0)))
    (do  (println "No more enemies. Iter" iter)
        ;(println grid)
        ;(println (get-units grid :goblin :elf))
        ;(print-grid grid)
        [grid (dec iter)])
    (do (println iter)
        ;(println (map (fn [[y x u]] (:hp u)) (get-units grid :elf :goblin)))
        ;(println (get-units grid :goblin :elf))
        ;(println (get-units grid :goblin :elf))
        ;(print-grid grid)
        (recur (step grid elf-damage) (inc iter) elf-damage))))

(defn pt1 []
  (let [grid (read-input "src/advent_of_code_2018/day15/input.txt")
        elf-damage        3
        [final-grid iter] (simulate grid 0 elf-damage)
        units             (get-units final-grid :elf :goblin)
        hps               (map (fn [[y x u]] (:hp u)) units)]
    (println (* (apply + hps) iter))))

;(pt1)

(defn simulate-pt2 [grid iter elf-damage]
  (if (not (and (> (count (get-units grid :goblin)) 0)
                (> (count (get-units grid :elf)) 0)))
    (do  (println "No more enemies. Iter" iter)
        [grid (dec iter) elf-damage])
    (do (println iter)
        (let [new-grid (step grid elf-damage)]
          (if (not= (count (get-units grid :elf)) (count (get-units new-grid :elf)))
            nil
            (recur (step grid elf-damage) (inc iter) elf-damage))))))

(defn pt2 []
  (let [grid (read-input "src/advent_of_code_2018/day15/input.txt")
        [final-grid iter elf-damage] (first (filter some?
                                         (map (fn [elf-damage] (simulate-pt2 grid 0 elf-damage))
                                                               (drop 3 (range)))))
        units             (get-units final-grid :elf)
        hps               (map (fn [[y x u]] (:hp u)) units)]
    (println elf-damage (* (apply + hps) iter))))

(pt2)
