(ns segmentbuilder.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY routes]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [ring.util.response :refer [resource-response]]
            [clojure.java.io :as io]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :as basic]
            [cemerick.drawbridge :as drawbridge]
            [environ.core :refer [env]]
            [segmentbuilder.strava :as strava]))

(def oauth_state "random")

(defn- authenticated? [user pass]
  ;; TODO: heroku config:add REPL_USER=[...] REPL_PASSWORD=[...]
  ; test locally with http://user:password@localhost:5000/repl
  (= [user pass] [(env :repl-user false) (env :repl-password false)]))

(def ^:private drawbridge
  (-> (drawbridge/ring-handler)
      (session/wrap-session)
      (basic/wrap-basic-authentication authenticated?)))

(defroutes app
  (ANY "/repl" {:as req}
       (drawbridge req))
  (GET "/redirect" [state code :as {session :session}] 
    (if (and (= state oauth_state) (not (nil? code)))
      (if-let [oauth_token (strava/process-code-from-redirect code)]
        (let [r (assoc (resource-response "index.html" {:root "public"}) :session (assoc session :oauth_token oauth_token))]
          (println "response is" r)
          r)
        (resource-response "/login.html" {:root "public"}))
      {:status 403
       :headers {"Content-Type" "text/plain"}
       :body (str "incorrect state returned was" state)}))
  (GET "/" [:as {session :session}]
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body (pr-str ["Hello" :from 'Heroku session])})
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defroutes resource-routes
    (route/resources "/"))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "500.html"))}))))

(def app_resources (routes resource-routes app))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))
        ;; TODO: heroku config:add SESSION_SECRET=$RANDOM_16_CHARS
        store (cookie/cookie-store {:key (env :session-secret)})]
    (jetty/run-jetty (-> #'app_resources
                         ((if (env :production)
                            wrap-error-page
                            trace/wrap-stacktrace))

                         (site {:session {:store store}}))
                     {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))