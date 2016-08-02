(ns molder.test.input.csv-input
  (:use [clojure.test]
        [molder.core]
        [molder.node-defs])
  (:require [molder.nodes.input.csv-input :as csv-input]))

(def test-input-csv-expected1
    '({:Name "Kasper" :Age "31" :Country "Switzerland"}
      {:Name "Kevin" :Age "31" :Country "Denmark"}
      {:Name "Santa Clause" :Age "800" :Country "North Pole"}))

(def test-input-csv-expected2
    '({:header1 "Kasper" :header2 "31" :header3 "Switzerland"}
      {:header1 "Kevin" :header2 "31" :header3 "Denmark"}
      {:header1 "Santa Clause" :header2 "800" :header3 "North Pole"},
      {:header1 "Mike Smith", :header2 "", :header3 ""}))

;Test input-csv function
(deftest test-input-csv-implementation
    ;read full file with headers and semi-colon separation
    (is (= (csv-input/csv-input-impl "test/molder/test/data/smallset.csv" \; true) test-input-csv-expected1))
    ;read only 2 entries from file with headers and semi-colon separation
    (is (= (csv-input/csv-input-impl "test/molder/test/data/smallset.csv" \; true 2) (take 2 test-input-csv-expected1)))
    ;read more entries from file than exists
    (is (= (csv-input/csv-input-impl "test/molder/test/data/smallset.csv" \; true 200) test-input-csv-expected1))
    ;read full file with no headers and comma separation and some empty fields
    (is (= (csv-input/csv-input-impl "test/molder/test/input/data/testdata-noheader.csv" \, false) test-input-csv-expected2)))

(def nodedef1
    { :type :csv-input,
      :id "csv-in14"
      :fields
      { :filename "test/molder/test/data/smallset.csv"
        :header true
        :separator \; },
      :name "Adresses CSV"})

(def nodedef2
    { :type "csv-input",
      :id "csv-in18"
      :fields
      { :filename "test/molder/test/input/data/testdata-noheader.csv"
        :header false
        :separator \, },
      :name "Adresses CSV"})

;Test multi-method overwrite and parameters
(deftest test-run-node-multimethod
    (is (= (run-node nodedef1) test-input-csv-expected1))
    (is (= (run-node nodedef2) test-input-csv-expected2)))

; Check metadata multimethod is properly overwritten
(deftest test-metadata-multimethod
  (is (= (node-metadata :csv-input) csv-input/metadata)))

