(ns segmentbuilder.dropbox
  (:use     [clojure.pprint])
  (:require [clojure.data.json          :as json]
            [clj-http.client            :as http]))

(def client_id "7kmzgqwz1ehxrwm")
(def client_secret "74vqtad4skkufi4")

(defn process-code-from-redirect 
  "returns the access_token"
  [code]
  (println "processing code" code)
  (->
    (http/post "https://api.dropbox.com/1/oauth2/token"
      {:form-params 
        {:client_id client_id
         :client_secret client_secret
         :code code
         :grant_type   "authorization_code"
         :redirect_uri "http://localhost:5000/dropbox/redirect"}})
    :body
    (json/read-json)
    :access_token
    ))

(def token "Sihum6rFJDoAAAAAAAAEJzeeaxepHVZ6E61uE8MF8wmyL81O16si4PxzqR_35xi3")

(defn- get-data [url token]
  (->
    (http/get url
      {:headers {"authorization" (str "Bearer " token)}
       :throw-exceptions false})
    :body
    (json/read-json)))

(defn user [token]
  (println "dropbox oauth token is:  " token)
  (get-data "https://api.dropbox.com/1/account/info" token))

(defn file [token url]
  (->
    (http/get url
      {:headers {"authorization" (str "Bearer " token)}
       :throw-exceptions false})
    :body))

;(user token)
;(file token)