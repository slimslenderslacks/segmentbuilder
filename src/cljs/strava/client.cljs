(ns strava.client
  (:require [domina     :as d]
    [domina.xpath       :as dx]
    [goog.net.XhrIo     :as xhr]
    [domina.events      :as events]
    [clojure.string     :as str]))


; (defn update-dom-resource 
;   "alters the DOM with a new set of resources from the server"
;   [resource json]
;   (let [data     (js->clj (.getResponseJson (.-target json)) :keywordize-keys true)
;         sel-div  (dx/xpath (str "//div[@id='" resource "-selection']")) 
;         bc-div   (dx/xpath "//ul[@class='breadcrumb']")]
;     (d/destroy-children! sel-div)
;     (d/append! sel-div (v/add-resource-form resource data @orgs))
;     (update-dom-breadcrumb ["home" resource (:id data)])))

(defn ^:export main []
  
  (xhr/send (str "user") 
    (fn [json]
      (let [data (js->clj (.getResponseJson (.-target json)) :keywordize-keys true)]
        (.log js/console "user" (str data))
        (d/set-text! (d/by-id "user") (str (:firstname data) " " (:lastname data)))
        (d/set-attr! (d/by-id "image") "src" (:profile_medium data))))
  ; (events/listen! (dx/xpath "//a[@data-toggle='tab']")
  ;   :click (fn [event]
  ;             (let [name (get-tab-id (str (:target event)))]
  ;               (.log js/console "target of event is " (str (:target event)))
  ;               (.log js/console "caught a show event for " name)
  ;               (xhr/send (str "api/v1/" name) (partial update-dom-resources name))
  ;               (update-dom-forms name)
  ;               (events/prevent-default event))))
  ))