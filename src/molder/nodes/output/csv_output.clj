; TODO validation function for invalid separator and filename parameters
; TODO Error handling file output impossible

(ns molder.nodes.output.csv-output)
(require '[clojure.data.csv :as csv]
         '[clojure.java.io :as io]
         '[molder.utils :as utils])

(defn csv-output-impl
    "Writes to .csv file (comma separated values)

    filename: the file to write (string)
    separator: the separator of the csv file (char)
    header: true if the first line of the .csv file should be a headers (boolean)"
    [ filename separator header table ]
    (let
        [ data-body (:data table)
          out-data (if header
                      (cons (utils/headers-to-list (:columns table)) data-body)
                      data-body) ]
        (with-open [out-file (io/writer filename)] (csv/write-csv out-file out-data :separator separator))))

(defn csv-output [node table]
  (let [fields (:fields node)
        filename (:filename fields)
        separator (:separator fields)
        header (:header fields)
        ; TODO proper test, warnings, etc for separator and header being correct types
        separator-char (if (char? separator) separator (first separator)) ]
    ; (println "ORIG TABLES: " tables)))
    (csv-output-impl filename separator-char header table)))

(def metadata
  { :in-points 1
    :out-points 0
    :type-name "CSV Output"
    :type "csv-output"
    :description "Writes .csv (comma separated values) files. Supports other separators than commas"
    :fields
        { :filename
            { :type "file"
              :required true
              :default ""
              :name "Filename"
              :tooltip "The full filename of the .CSV file to write to" }
          :header
            { :type "boolean"
              :required true
              :default true
              :name "Has header"
              :tooltip "Should be true (marked) if the first line of the CSV file should be the column headers/titles" }
          :separator
            { :type "char"
              :required true
              :default ","
              :name "Separator",
              :tooltip "The character separating the entries in the CSV file" }}})