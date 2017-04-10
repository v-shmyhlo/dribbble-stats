(ns dribbble-stats.core
  (:require [clojure.string :as string]
            [clojure.core.async :as a]
            [dribbble-stats.utils :refer :all]
            [clojure.tools.cli :refer [parse-opts]]
            [dribbble-stats.dribbble-client :as api])
  (:gen-class))

(defn exit [code message]
  (println message)
  (System/exit code))

(defn usage [summary]
  summary)

(def cli-options
  [[nil "--access-token TOKEN" "Dribbble API Access Token"]
   [nil "--user USER" "Dribbble User Name"]])

(defn validate-args [args]
  (cond
    (:errors args) (exit 1 (string/join (:errors args)))
    (or (empty? (:user (:options args)))
        (empty? (:access-token (:options args)))) (exit 0 (usage (:summary args)))
    :else args))

(defn make-top-likers-chan [user-id params]
  (let [user (:body (a/<!! (api/make-response-chan api/get-user user-id params)))
        followers-chan (api/make-user-followers-chan user params)
        shots-chan (chan-flatmap (fn [v] (api/make-user-shots-chan (:follower v) params))
                                 followers-chan)
        likes-chan (chan-flatmap (fn [shot] (api/make-shot-likes-chan shot params))
                                 shots-chan)]
    (->> likes-chan
         (a/reduce (fn [memo {:keys [user]}]
                     (update memo (:username user) (fnil inc 0)))
                   {})
         (a/<!!)
         (into [])
         (sort-by second #(compare %2 %1))
         (take 10))))

(defn print-top-likers [xs]
  (run! (fn [[username, likes-count]] (println username "-" likes-count))
        xs))

(defn -main
  [& args]
  (-> args
      (parse-opts cli-options :strict true)
      (validate-args)
      (:options)
      ((fn [{:keys [access-token user]}]
         (make-top-likers-chan user {:access-token access-token})))
      (print-top-likers)))
