(ns segmentbuilder.strava
  (:use     [clojure.pprint])
  (:require [mm.client                  :as client]
            [clojure.data.json          :as json]
            [clj-http.client            :as http]))

(def client_id 1422)
(def client_secret "609ff2e39de6b06905faf04b3889cf944e77fcb1")

; https://www.strava.com/oauth/authorize?client_id=1422&response_type=code&redirect_uri=http://localhost:3000/redirect&scope=write&state=myState

(pprint
  (client/get "https://www.strava.com/oauth/authorize"
    {:query-params 
      {:client_id 1422
       :response_type "code"
       :redirect_uri "http://localhost:3000/redirect"
       :scope "write"
       :state "myState"
       :approval_prompt "auto"}}))

(pprint
  (http/post "https://www.strava.com/oauth/token"
    {:form-params 
      {:client_id client_id
       :client_secret client_secret
       :code "4d4dfed207fd1b866fe8b394abdeb02a30f9230b"}}))

(def access_token "5d6da0a9fef00b76fb2ed47909783a18c1e893a3")

(defn- get-data [url]
  (->
    (http/get url
      {:headers {"authorization" (str "Bearer " access_token)}
       :throw-exceptions false})
    :body
    (json/read-json)))

(pprint (get-data "https://www.strava.com/api/v3/athlete"))
(pprint (get-data "https://www.strava.com/api/v3/segments/starred"))
(pprint (get-data "https://www.strava.com/api/v3/segments/1334338"))
