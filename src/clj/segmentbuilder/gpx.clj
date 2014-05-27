(ns segmentbuilder.gpx
  (:use [clojure.pprint]
        [clojure.walk]
        [clojure.test])
  (:import [java.lang Math])
  (:require [clojure.data.xml :as xml]
            [segmentbuilder.dropbox :as dropbox]))

(def mode :local)

(defn node [e]
  (:str e))

(defn parent [e children-seq]
  (when (and 
          (= (:type e) :start-element) 
        )
    (cond
      (= (:name e) :trkseg) [:trkseg {}]
      (= (:name e) :name)   [:name {} (str "transform:  " (first children-seq))]
      (= (:name e) :time)   [:time {} (str "transform:  " (first children-seq))]
      :else                 [(:name e) (stringify-keys (:attrs e)) children-seq])))

(defmulti read-gpx (fn [url] mode))
(defmethod read-gpx :local 
  [url]
  (java.io.BufferedReader. (java.io.FileReader. "test.gpx")))
(defmethod read-gpx :dropbox
  [url]
  (dropbox/file dropbox/token url))

(defn distance 
  [[x1 y1] [x2 y2]]
  (let [y (- y2 y1) x (- x2 x1)]
    (Math/sqrt (+ (* x x) (* y y)))))

(deftest test-distance
  (is (= 1.0 (distance [0 0] [0 1])))
  (is (= 1.0 (distance [0 0] [1 0])))
  (is (= 1.0 (distance [0 1] [0 0])))
  (is (= 1.0 (distance [1 0] [0 0])))
  (is (= 10.0 (distance [10 0] [0 0])))
  (is (= 10.0 (distance [10.0 0.0] [0.0 0.0])))
  )

(def state [{:point [0.0 0.0] :nearest nil :distance nil}])
(def points [[-4.0 0.0] [-3.0 0.0] [-2.0 0.0] [-1.0 0.0] [0.0 0.0] [1.0 0.0] [2.0 0.0]])

(defn find-nearest [state point]
  (let [dist (distance (:point state) point)]
    (if (nil? (:distance state))
      (assoc state :distance dist :nearest point)
      (cond
        (< (:distance state) dist) state
        (> (:distance state) dist) (assoc state :distance dist :nearest point)
        :else                      state))))

(reduce find-nearest (first state) points)

(run-tests *ns*)

(def url "https://dl.dropboxusercontent.com/1/view/esg9kbznlh1vreg/gpx/test.gpx")

(defn write-gpx [gpx-url segment-id]
  (println "request segment " segment-id " from " gpx-url)
  (spit "crap.xml"
    (xml/indent-str
      (xml/sexp-as-element
        (first
          (first
            (let [s (xml/source-seq (read-gpx gpx-url))]
              (xml/seq-tree parent #(= (:type %) :end-element) node s))))))))

; gpx
;   metadata
;     time
;   trk
;     name
;     trkseg
;       trkpt {:lat :lon}
;         ele
;         time
;         extensions
;           gpxtpx:TrackPointExtension
;             gpxtpx:atemp
;             gpxtpx:hr
;             gpxtpx:cad