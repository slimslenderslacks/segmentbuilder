(ns segmentbuilder.gpx
  (:use [clojure.pprint]
        [clojure.walk])
  (:require [clojure.data.xml :as xml]
            [segmentbuilder.dropbox :as dropbox]))

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

(spit "crap.xml"
  (xml/indent-str
    (xml/sexp-as-element
      (first
        (first
          (let [s (xml/source-seq (java.io.BufferedReader. (java.io.FileReader. "test.gpx")))]
            (xml/seq-tree parent #(= (:type %) :end-element) node s)))))))

// gpx
//   metadata
//     time
//   trk
//     name
//     trkseg
//       trkpt {:lat :lon}
//         ele
//         time
//         extensions
//           gpxtpx:TrackPointExtension
//             gpxtpx:atemp
//             gpxtpx:hr
//             gpxtpx:cad