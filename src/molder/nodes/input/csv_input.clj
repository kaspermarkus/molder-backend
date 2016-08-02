; TODO error handling in case the CSV file is malformatted (ie. too few or too many separators on each line)

(ns molder.nodes.input.csv-input)
(require '[clojure.data.csv :as csv]
         '[clojure.java.io :as io])

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
            headers (if header
                ;read first line of file and change values to keywords in a list
                (map (fn [val] (keyword (clojure.string/trim val))) (first full-input))
                ;else make up our own headers
                (map (fn [ num ] (keyword (str "header" num))) (iterate inc 1)))]
                    (filter identity (map (fn [row] ;filter to ensure no nil values
                (if (not (and (= [""] row) (= (count row) 1))) ;ignore empty lines (make nil)
                    (apply hash-map (interleave headers row)))) data))))
    ; Limit number of lines taken by supplying it as extra parameter
    ([filename separator header numlines]
        (take numlines (csv-input-impl filename separator header))))

(defn csv-input [node]
  (let [fields (:fields node)
        filename (:filename fields)
        separator (:separator fields)
        header (:header fields) ]
    (csv-input-impl filename separator header)))

(def metadata
  { :in-points 0
    :out-points 1
    :type-name "CSV Input"
    :type :csv-input
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
              :default \;
              :name "Separator",
              :tooltip "The character separating the entries in the CSV file" }}})