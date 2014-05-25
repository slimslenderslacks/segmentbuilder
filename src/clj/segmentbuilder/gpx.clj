(ns segmentbuilder.gpx
  (:require [clojure.data.xml :as xml]
            [segmentbuilder.dropbox :as dropbox]))

(with-open [input (java.io.StringReader. (dropbox/file dropbox/token))]
  (let [s (xml/source-seq input)]
    (doseq [e s]
      (println e))))

(with-open [input (java.io.FileInputStream. "test.gpx")]
  (let [s (xml/parse input)]
    (doseq [e s]
      (println e))))

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