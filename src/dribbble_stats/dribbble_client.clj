(ns dribbble-stats.dribbble-client
  (:require [clj-http.client :as client]
            [clojure.core.async :as a]
            [clj-time.core :as t]
            [clj-time.coerce :as tc])
  (:use [slingshot.slingshot :only [throw+ try+]])
  (:gen-class))

(defn api-get [endpoint {:keys [access-token page per-page]}]
  (client/get
    (str "https://api.dribbble.com/v1/" endpoint)
    {:query-params {:access_token access-token,
                    :page page,
                    :per_page per-page},
     :insecure? true,
     :as :json}))

(defn get-user [id params]
  (api-get (str "users/" id) params))

(defn get-shot [id params]
  (api-get (str "shots/" id) params))

(defn get-user-followers [id params]
  (api-get (str "users/" id "/followers") params))

(defn get-user-shots [id params]
  (api-get (str "users/" id "/shots") params))

(defn get-shot-likes [shot params]
  (api-get (str "shots/" shot "/likes") params))

(defn handle-rate-limit [f & args]
  (try+
    [true (apply f args)]
    (catch [:status 429] {:keys [headers]}
      (let [rate-limit-reset (-> headers (:X-RateLimit-Reset) (Long/parseLong) (* 1000))
            now (tc/to-long (t/now))]
        [false (- rate-limit-reset now)]))))

(defn make-response-chan [& args]
  (let [c (a/chan)]
    (a/go-loop []
               (let [[success? result] (apply handle-rate-limit args)]
                 (if success?
                   (do
                     (a/>! c result)
                     (a/close! c))
                   (do
                     (a/<! (a/timeout result))
                     (recur)))))
    c))

(defn paginate-params [params page per-page]
  (-> params
      (assoc :page page)
      (assoc :per-page per-page)))

; TODO: rename
(defn make-items-chan [n f id params]
  (let [c (a/chan)
        pages (Math/ceil (/ n 100))
        responses-chan (a/merge (map #(make-response-chan f id (paginate-params params % 100))
                                     (range 1 (+ 1 pages))))]
    (a/pipeline-async 8
                      c
                      (fn [value result]
                        (a/onto-chan result (:body value)))
                      responses-chan)
    c))

(defn make-user-followers-chan [user params]
  (make-items-chan (:followers_count user)
                   get-user-followers
                   (:id user)
                   params))

(defn make-user-shots-chan [user params]
  (make-items-chan (:shots_count user)
                   get-user-shots
                   (:id user)
                   params))

(defn make-shot-likes-chan [shot params]
  (make-items-chan (:likes_count shot)
                   get-shot-likes
                   (:id shot)
                   params))
