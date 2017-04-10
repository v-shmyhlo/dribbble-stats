(ns dribbble-stats.url-match-test
  (:require [clojure.test :refer :all]
            [dribbble-stats.url-match :refer :all]))

(deftest recognize-test
  (let [pattern (new-pattern "host(twitter.com); path(?user/status/?id);")]
    (is (= [[:user "bradfitz"] [:id "562360748727611392"]]
           (recognize pattern "http://twitter.com/bradfitz/status/562360748727611392"))))

  (let [pattern (new-pattern "host(dribbble.com); path(shots/?id); queryparam(offset=?offset);")]
    (is (= [[:id "1905065-Travel-Icons-pack"] [:offset "1"]]
           (recognize pattern "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users&offset=1")))
    (is (nil? (recognize pattern "https://twitter.com/shots/1905065-Travel-Icons-pack?list=users&offset=1")))
    (is (nil? (recognize pattern "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users"))))

  (let [pattern (new-pattern "host(twitter.com); path(shots/?id); queryparam(offset=?offset-dashed); queryparam(list=?type_underscored);")]
    (is (= [[:id "1905065-Travel-Icons-pack"] [:offset-dashed "1"] [:type_underscored "users"]]
           (recognize pattern "https://twitter.com/shots/1905065-Travel-Icons-pack?list=users&offset=1")))
    (is (nil? (recognize pattern "https://twitter.com/shots/1905065-Travel-Icons-pack?list=users")))
    (is (nil? (recognize pattern "https://twitter.com/shots/1905065-Travel-Icons-pack?offset=1"))))

  (let [pattern (new-pattern "host(dribbble.com); path(shots/?id/likes); queryparam(offset=?offset); queryparam(list=users);")]
    (is (= [[:id "1905065-Travel-Icons-pack"] [:offset "1"]]
           (recognize pattern "https://dribbble.com/shots/1905065-Travel-Icons-pack/likes?list=users&offset=1")))
    (is (nil? (recognize pattern "https://dribbble.com/shots/1905065-Travel-Icons-pack/likes?list=shots&offset=1")))
    (is (nil? (recognize pattern "https://dribbble.com/shots/1905065-Travel-Icons-pack/likes?list=users")))
    (is (nil? (recognize pattern "https://dribbble.com/shots/1905065-Travel-Icons-pack/likes?offset=1"))))

  (let [pattern (new-pattern "")]
    (is (= []
           (recognize pattern "https://dribbble.com/shots/1905065-Travel-Icons-pack/likes?list=users&offset=1")))))
