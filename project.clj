(defproject segmentbuilder "1.0.0-SNAPSHOT"
  :description "SegmentBuilder Strava Application"
  :url "http://segmentbuilder.herokuapp.com"
  :license {:name "FIXME: choose"
            :url "http://example.com/FIXME"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.1"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [ring/ring-devel "1.1.0"]
                 [ring-basic-authentication "1.0.1"]
                 [environ "0.2.1"]
                 [com.cemerick/drawbridge "0.0.6"]
                 [org.clojure/data.json         "0.2.1"]
                 [clj-http                      "0.7.7"]             ; wraps Apache httpComponents
                 [ring/ring-json                "0.1.2"]             ; ring middleware

                 [org.clojure/data.xml          "0.0.7"]
                 [org.clojure/data.json         "0.2.1"]
                 
                ]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"][lein-ring                     "0.8.5"]]
  :hooks [environ.leiningen.hooks]
  :env {:repl-user "slim" :repl-password "welcome1"}
  :source-paths ["src/clj"]
  :profiles {
    :production {:env {:production true}}
    :clj  {:source-paths ["src/clj"]}
    :cljs {:dependencies [
                 [org.clojure/clojurescript     "0.0-1450"]
                 [domina                        "1.0.0"]
                 [hiccups                       "0.1.1"]]
           :plugins [                 
                 [lein-cljsbuild                "0.3.2"]
                 ] 
           :cljsbuild {
             :builds [
               {
                 :source-paths ["src/cljs"]
                 :compiler {
                   :output-to "resources/public/client.js"
                   :optimizations :whitespace
                   :pretty-print true}}
              ]
            }
            :ring     {:handler segmentbuilder.web/-main} ; only relevant when the lein-ring plugin is active
            :main segmentbuilder.web
          }
    })
