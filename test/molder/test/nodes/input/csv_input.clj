(ns molder.test.nodes.input.csv-input
  (:use [clojure.test]
        [molder.core]
        [molder.node-defs])
  (:require
        [molder.nodes.input.csv-input :as csv-input]
        [molder.test.data.simpletables :as test-tables]))

(def test-input-csv-expected2
    { :columns
     { :column1 { :index 0 }
       :column2 { :index 1 }
       :column3 { :index 2 }}
     :data
       '( [ "Kasper" "31" "Switzerland" ]
          [ "Kevin" "31" "Denmark" ]
          [ "Santa Clause" "800" "North Pole" ]
          [ "Mike Smith" "" "" ]) })

;Test input-csv function
(deftest test-input-csv-implementation
    ;read full file with headers and semi-colon separation
    (is (= test-tables/tinytable1 (csv-input/csv-input-impl "test/molder/test/data/smallset.csv" \; true)))
    ;read only 2 entries from file with headers and semi-colon separation
    (is (= { :columns (:columns test-tables/tinytable1) :data (take 2 (:data test-tables/tinytable1)) }
           (csv-input/csv-input-impl "test/molder/test/data/smallset.csv" \; true 2)))
    ;read more entries from file than exists
    (is (= test-tables/tinytable1 (csv-input/csv-input-impl "test/molder/test/data/smallset.csv" \; true 200)))
    ;read full file with no headers and comma separation and some empty fields
    (is (= test-input-csv-expected2 (csv-input/csv-input-impl "test/molder/test/nodes/input/data/testdata-noheader.csv" \, false))))

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
      { :filename "test/molder/test/nodes/input/data/testdata-noheader.csv"
        :header false
        :separator \, },
      :name "Adresses CSV"})

;Test multi-method overwrite and parameters
(deftest test-run-node-multimethod
    (is (= (run-node nodedef1) test-tables/tinytable1))
    (is (= (run-node nodedef2) test-input-csv-expected2)))

; Check metadata multimethod is properly overwritten
(deftest test-metadata-multimethod
  (is (= (node-metadata :csv-input) csv-input/metadata)))

