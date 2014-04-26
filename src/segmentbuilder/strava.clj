(ns segmentbuilder.strava
  (:use     [clojure.pprint])
  (:require [clojure.data.json          :as json]
            [clj-http.client            :as http]))

(def client_id 1422)
(def client_secret "609ff2e39de6b06905faf04b3889cf944e77fcb1")

; https://www.strava.com/oauth/authorize?client_id=1422&response_type=code&redirect_uri=http://localhost:3000/redirect&scope=write&state=myState

(pprint
  (http/get "https://www.strava.com/oauth/authorize"
    {:query-params 
      {:client_id 1422
       :response_type "code"
       :redirect_uri "http://localhost:3000/redirect"
       :scope "write"
       :state "myState"
       :approval_prompt "auto"}}))

; when you authorize, you'll get a new code each time
; f04fce193a4fa502b6dc39c1664cae22008e6f22
; c51f9cd5d501c0f6784d067e07309c1cc57dc5b8
; c62614dde213753b0873144c58255f5bab4a4258
; when you exchange the code for an access_token, 
; you get the same token if you repeat the request multiple times
; even if the access code changes, the same token is returned

(defn process-code-from-redirect 
  "returns the access_token"
  [code]
  (->
    (http/post "https://www.strava.com/oauth/token"
      {:form-params 
        {:client_id client_id
         :client_secret client_secret
         :code code}})
    :body
    (json/read-json)
    :access_token
    ))

(process-code-from-redirect "c62614dde213753b0873144c58255f5bab4a4258")

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
