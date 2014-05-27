(ns strava.client
  (:require 
    [hiccups.runtime    :as hiccupsrt]
    [domina             :as d]
    [domina.xpath       :as dx]
    [goog.net.XhrIo     :as xhr]
    [domina.events      :as events]
    [clojure.string     :as str])
  (:require-macros [hiccups.core :as hiccups]))

(hiccups/defhtml segment-row
  [{:keys [name start_latlng end_latlng id] :as data} segment]
  [:tr {:id id}
    [:td {:id id} id]
    [:td {:id id} name]
    [:td {:id id} (first start_latlng)]
    [:td {:id id} (second start_latlng)]
    [:td {:id id} (first end_latlng)]
    [:td {:id id} (second end_latlng)]
  ])
; keys [dropbox-user strava-user gpx-url segment-coords segment-id]
(def state (atom {}))

(defn clj->js
  "Recursively transforms ClojureScript maps into Javascript objects,
   other ClojureScript colls into JavaScript arrays, and ClojureScript
   keywords into JavaScript strings."
  [x]
  (cond
    (string? x) x
    (keyword? x) (name x)
    (map? x) (.-strobj (reduce (fn [m [k v]]
                       (assoc m (clj->js k) (clj->js v))) 
                     {} x))
    (coll? x) (apply array (map clj->js x))
    :else x))

(defn ^:export filesSelected [files]
  (doseq [k (keys (first (js->clj files)))]
    (.log js/console "key->" k))
  (swap! state assoc :gpx-url (get (first (js->clj files)) "link"))
  (d/set-text! (d/by-id "selected") (str "Selected Resource:  " (:gpx-url @state))))

(defn- generate []
  (xhr/send (str "/generate")
    (fn [e] 
      ; TODO error handler and progress
      )
    "POST"
    (str/join "&" (map (fn [[k v]] (str (name k) "=" v)) (select-keys @state [:gpx-url :segment-id])))
    (clj->js {"Content-Type" "application/x-www-form-urlencoded"})
    ))

(defn ^:export main []
  
  (xhr/send (str "/user") 
    (fn [json]
      (let [data (js->clj (.getResponseJson (.-target json)) :keywordize-keys true)]
        (.log js/console "user" (str data))
        (swap! state assoc :strava-user (str (:firstname data) " " (:lastname data)))
        (d/set-text! (d/by-id "strava-user") (:strava-user @state))
        (d/set-attr! (d/by-id "strava-photo") "src" (:profile_medium data)))))

  (xhr/send (str "/dropbox/user") 
    (fn [json]
      (let [data (js->clj (.getResponseJson (.-target json)) :keywordize-keys true)]
        (.log js/console "user" (str data))
        (swap! state assoc :dropbox-user (:display_name data))
        (d/set-text! (d/by-id "dropbox-user") (:dropbox-user @state))
        )))

  (xhr/send (str "/segments")
    (fn [json]
      (let [data (js->clj (.getResponseJson (.-target json)) :keywordize-keys true)
            sel-div (dx/xpath "//tbody[@id='data']")]
        (d/destroy-children! sel-div)
        (d/append! sel-div (str (map segment-row data)))
        (events/listen! (dx/xpath "//tr")
          :click (fn [event] 
                   (.log js/console "selected a row " (.-id (:target event)) " -> " (js->clj (:target event)) "...")
                   (swap! state assoc :segment-id (.-id (:target event)))))
        )))

  (events/listen! (dx/xpath "//button[@id='generate']")
    :click (fn [event] 
      (if (and (:segment-id @state) (:gpx-url @state)) 
        (generate)
        (js/alert "must have previous selected a dropbox gpx file and a Strava segment"))))

  )