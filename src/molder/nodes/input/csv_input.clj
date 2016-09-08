; TODO error handling in case the CSV file is malformatted (ie. too few or too many separators on each line)
; TODO error handling in case of empty file
; TODO error handling in case of missing file
(ns molder.nodes.input.csv-input)
(require '[clojure.data.csv :as csv]
         '[clojure.java.io :as io]
         '[molder.error-handling :as errors]
         '[molder.utils :as utils])

(defn parse-headers [headers? first-line] ; TODO test
  (if headers?
    (utils/headers-from-list first-line)
    (apply merge (map (fn [i] { (keyword (str "column" (inc i))) { :index i}}) (range (count first-line))))))

(defn csv-input-impl
    "Reads .csv file (comma separated values) and returns the data.

    filename: the file to read (string)
    separator: the separator of the csv file (char)
    header: true if the first line of the .csv file contains headers (boolean)
    numlines: OPTIONAL - the number of lines to read from output"
    ([ filename separator header ]
        (let [
            full-input (with-open [in-file (io/reader filename)] (doall (csv/read-csv in-file :separator separator)))
            data (if header (rest full-input) full-input)
            headers (parse-headers header (first full-input))
            filtered-data (filter (fn [row] (= (count row) (count headers))) data)]
          { :columns headers :data filtered-data }))
    ; Limit number of lines taken by supplying it as extra parameter
    ([filename separator header numlines]
        (let [orig (csv-input-impl filename separator header)]
          { :columns (:columns orig) :data (take numlines (:data orig)) })))

; TODO tests
(defn parse-separator [separator]
  (if (char? separator)
    separator
    (if (= separator "\\t")
      \tab
      (first separator))))

(defn csv-input [node]
  (let [fields (:fields node)
        filename (:filename fields)
        separator (:separator fields)
        header (:header fields)
        separator-char (parse-separator separator)]
    (csv-input-impl filename separator-char header)))

(defn validate [ node state ]
  (let [ fields (:fields node)
         filename (:filename fields)
         separator (:separator fields)]
    ; check separator
    (if (not (char? separator))
      (do
        (if (and (< 1 (count separator)) (not (= separator "\\t")))
          (errors/add-parameter-error state node :separator
            "CSV Input node retrieved the separator: '" separator "' which is more than one character long"))
        (if (= 0 (count separator))
          (errors/add-parameter-error state node :separator "CSV Input node retrieved an empty separator"))))
    ; check that file exists
    (if (not (.exists (io/file filename)))
      (errors/add-parameter-error state node :filename
            "CSV Input node retrieved an invalid filename (" filename ") - it does not exist"))))

(def metadata
  { :in-points 0
    :out-points 1
    :type-name "CSV Input"
    :type "csv-input"
    :description "Reads .csv (comma separated values) files. Accepts other separators than commas"
    :fields
        { :filename
            { :type "file"
              :required true
              :default ""
              :name "Filename"
              :tooltip "The full filename of the .CSV file to read from" }
          :header
            { :type "boolean"
              :required true
              :default true
              :name "Has header"
              :tooltip "Should be true (marked) if the first line of the CSV file is the column headers/titles" }
          :separator
            { :type "char"
              :required true
              :default ","
              :name "Separator",
              :tooltip "The character separating the entries in the CSV file" }}})