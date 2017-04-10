(ns dribbble-stats.utils-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as a]
            [dribbble-stats.utils :refer :all]))

(deftest chan-flatmap-test
  (is (= [1 1 1 2 2 2 3 3 3]
         (chan-to-seq (chan-flatmap (fn [v] (a/to-chan [v v v]))
                                    (a/to-chan [1 2 3]))))))
