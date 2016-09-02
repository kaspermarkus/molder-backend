(ns molder.test.nodes.output.csv-output
  (:use [clojure.test]
        [molder.core]
        [molder.node-defs])
  (:require
    [molder.nodes.output.csv-output :as csv-output]
    [molder.test.data.simpletables :as test-tables]
    [clojure.java.io :as io]))

(def test-output-csv-expected1
    "Name;Age;Country\nKasper;31;Switzerland\nKevin;31;Denmark\nSanta Clause;800;North Pole\n")

(def test-output-csv-expected2
    "Kasper,31,Switzerland\nKevin,31,Denmark\nSanta Clause,800,North Pole\n")

;Test output-csv function
(deftest test-csv-output-impl
    ;write file with headers and semi-colon separation
    (csv-output/csv-output-impl "test/molder/test/nodes/output/data/tmp-out.csv" \; true test-tables/tinytable1)
    (is (= test-output-csv-expected1 (slurp  "test/molder/test/nodes/output/data/tmp-out.csv")))

    ;write file with headers and semi-colon separation
    (csv-output/csv-output-impl "test/molder/test/nodes/output/data/tmp-out2.csv" \, false test-tables/tinytable1)
    (is (= test-output-csv-expected2 (slurp  "test/molder/test/nodes/output/data/tmp-out2.csv")))

    ; ;cleanup
    (io/delete-file (io/file "test/molder/test/nodes/output/data/tmp-out.csv"))
    (io/delete-file (io/file "test/molder/test/nodes/output/data/tmp-out2.csv")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; MULTIMTEHOD STUFF TESTING ;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def nodedef1
    { :type :csv-output,
      :id "csv-out94"
      :fields
      { :filename "test/molder/test/nodes/output/data/tmp-out3.csv"
        :header true
        :separator \; },
      :name "Adresses CSV"})

(def nodedef2
    { :type "csv-output",
      :id "csv-out18"
      :fields
      { :filename "test/molder/test/nodes/output/data/tmp-out4.csv"
        :header false
        :separator \, },
      :name "Adresses CSV"})

;Test multi-method overwrite and parameters
(deftest test-run-node-multimethod-csv-output
    (run-node nodedef1 (list test-tables/tinytable1))
    (is (= test-output-csv-expected1 (slurp  "test/molder/test/nodes/output/data/tmp-out3.csv"))
        "Testing that multi-method overwriting works #1")

    (run-node nodedef2 (list test-tables/tinytable1))
    (is (= test-output-csv-expected2 (slurp  "test/molder/test/nodes/output/data/tmp-out4.csv"))
        "Testing that multi-method overwriting works #2")

    ; cleanup:
    (io/delete-file (io/file "test/molder/test/nodes/output/data/tmp-out3.csv"))
    (io/delete-file (io/file "test/molder/test/nodes/output/data/tmp-out4.csv")))

; Check metadata multimethod is properly overwritten
(deftest test-metadata-multimethod
  (is (= (node-metadata :csv-output) csv-output/metadata)))