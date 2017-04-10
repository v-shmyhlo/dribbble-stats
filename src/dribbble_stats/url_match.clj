(ns dribbble-stats.url-match
  (:require [clojure.string :as string]
            [clojure.algo.monads :as m]
            [cemerick.url :as u]))

(def pattern-part-regex #"\A(host|path|queryparam)\((.+)\)\z")

(defn parse-path [path]
  (string/split path #"\/"))

(defn parse-query-param [query-param]
  (string/split query-param #"="))

(defn binding? [value]
  (not (nil? (re-matches #"\A\?[\w\-]+" value))))

(defn parse-pattern-part [part]
  (let [[_ type contents] (re-matches pattern-part-regex part)]
    (case type
      "host" [:host contents]
      "path" [:path (parse-path contents)]
      "queryparam" [:query (parse-query-param contents)]
      (throw (Exception. (str "Invalid pattern part" part))))))

(defn new-pattern [pattern-string]
  (let [parts (->> (string/split pattern-string #";\s*")
                   (map string/trim)
                   (filter #(not (empty? %))))
        pattern (reduce (fn [memo part] (conj memo (parse-pattern-part part)))
                        []
                        parts)]
    pattern))

(defn recognize-host [host url]
  (when (= (:host url) host)
    []))

(defn match-or-bind [pattern string]
  (cond
    (nil? string) nil
    (binding? pattern) [[(keyword (subs pattern 1)) string]]
    (= pattern string) []))

(defn merge-bindings [a b]
  (m/domonad m/maybe-m
             [ma a
              mb b]
             (concat ma mb)))

(defn recognize-path [pattern-parts url]
  (let [path-parts (string/split (subs (:path url) 1) #"\/")]
    (when (= (count pattern-parts) (count path-parts))
      (reduce (fn [memo [pattern-part path-part]]
                (merge-bindings memo (match-or-bind pattern-part path-part)))
              []
              (zipmap pattern-parts path-parts)))))

(defn recognize-query [[param pattern] url]
  (match-or-bind pattern (get (:query url) param)))

(defn recognize-pattern-part [type value url]
  (case type
    :host (recognize-host value url)
    :path (recognize-path value url)
    :query (recognize-query value url)))

(defn recognize [pattern url-string]
  (let [url (u/url url-string)]
    (reduce (fn [memo [type value]]
              (merge-bindings memo (recognize-pattern-part type value url)))
            []
            pattern)))
