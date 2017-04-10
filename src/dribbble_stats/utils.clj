(ns dribbble-stats.utils
  (:require [clojure.core.async :as a]))

(defn print-chan
  "Prints all elements in channel and the number of elements"
  [c]
  (let [n (loop [n 0]
            (if-let [v (a/<!! c)]
              (do
                (println v)
                (recur (+ 1 n)))
              n))]
    (println "count:" n)))

(defn chan-to-seq [c]
  (a/<!! (a/reduce conj [] c)))

(defn chan-flatmap [f input]
  (let [output (a/chan)]
    (a/pipeline-async 8
                      output
                      (fn [value result] (a/pipe (f value) result))
                      input)
    output))
