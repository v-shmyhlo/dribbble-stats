(defproject dribbble_stats "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/core.async "0.3.442"]
                 [com.cemerick/url "0.1.1"]
                 [org.clojure/algo.monads "0.1.6"]
                 [clj-time "0.13.0"]
                 [clj-http "2.3.0"]
                 [cheshire "5.7.0"]]

  :main ^:skip-aot dribbble-stats.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
