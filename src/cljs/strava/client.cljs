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
  [:tr {}
    [:td {:role "whatever" :id (str "#" id)} id]
    [:td name]
    [:td (first start_latlng)]
    [:td (second start_latlng)]
    [:td (first end_latlng)]
    [:td (second end_latlng)]
  ])

(defn ^:export main []
  
  (xhr/send (str "user") 
    (fn [json]
      (let [data (js->clj (.getResponseJson (.-target json)) :keywordize-keys true)]
        (.log js/console "user" (str data))
        (d/set-text! (d/by-id "user") (str (:firstname data) " " (:lastname data)))
        (d/set-attr! (d/by-id "image") "src" (:profile_medium data)))))

  (xhr/send (str "segments")
    (fn [json]
      (let [data (js->clj (.getResponseJson (.-target json)) :keywordize-keys true)
            sel-div (dx/xpath "//tbody[@id='data']")]
        (d/destroy-children! sel-div)
        (d/append! sel-div (str (map segment-row data)))
        )))
  )