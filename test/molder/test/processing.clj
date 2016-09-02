(ns molder.test.processing
  (:use [clojure.test]
        [molder.processing]
        [molder.node-defs])
  (:require
    [clojure.java.io :as io]
    [molder.test.data.molds :as test-molds]
    [molder.test.data.simpletables :as test-tables]
    [molder.nodes.drop-columns :as drop-columns]))

(def tinytable1-namedropped (drop-columns/drop-columns-impl [ "Name" ] test-tables/tinytable1))

(deftest external-output-node?-tests
  (is (= false (external-output-node? (:csv-input5293 test-molds/test-nodes1)))
      "Check if input node evaluates to external output node")
  (is (= false (external-output-node? (:drop-columns123 test-molds/test-nodes1)))
      "Check if standard node evaluates to external output node")
  (is (= true (external-output-node? (:csv-output1892 test-molds/test-nodes1)))
      "Check if input node evaluates to external output node"))

(deftest is-missing-output-node?-tests
  (is (= false (is-missing-output-node? (:csv-input5293 test-molds/test-nodes1)))
      "Check if input node evaluates to false if it does have an output")
  (is (= false (is-missing-output-node? (:drop-columns123 test-molds/test-nodes1)))
      "Check if standard node evaluates to false if it does have an output")
  (is (= false (is-missing-output-node? (:csv-output1892 test-molds/test-nodes1)))
      "Check if external output node is interpreted correctly")
  (is (= true (is-missing-output-node? (:csv-input5293 test-molds/test-nodes3)))
      "Check if external input node evaluates to true if missing output")
  (is (= true (is-missing-output-node? (:drop-columns123 test-molds/test-nodes4)))
      "Check if transformation node evaluates to true if missing output"))

(deftest external-input-node?-tests
  (is (= true (external-input-node? (:csv-input5293 test-molds/test-nodes1)))
      "Check if input node evaluates to external input node")
  (is (= false (external-input-node? (:drop-columns123 test-molds/test-nodes1)))
      "Check if standard node evaluates to external input node")
  (is (= false (external-input-node? (:csv-output1892 test-molds/test-nodes1)))
      "Check if input node evaluates to external input node"))

(deftest filter-external-output-nodes-tests
  (is (= (list (:csv-output1892 test-molds/test-nodes1))
         (filter-external-output-nodes test-molds/test-nodes1))
      "Simple list with just one output")
  (is (= (list (:csv-output1892 test-molds/test-nodes2) (:csv-output1 test-molds/test-nodes2))
         (filter-external-output-nodes test-molds/test-nodes2))
      "List with two outputs"))

(deftest filter-nodes-to-try-tests
  (is (= (list
            (:drop-columns123 test-molds/test-nodes4)
            (:csv-output1 test-molds/test-nodes4))
         (filter-nodes-to-try test-molds/test-nodes4))
      "Filter with one external output and one incomplete"))

(deftest get-node-from-id-test
  (is (= (:csv-output1892 test-molds/test-nodes1)
         (get-node-from-id "csv-output1892" test-molds/test-nodes1))))

(deftest get-input-nodes-test
  (is (= (list (:drop-columns123 test-molds/test-nodes1))
         (get-input-nodes (:csv-output1892 test-molds/test-nodes1) test-molds/test-nodes1))))

(def state (atom { :data {} :errors {} :warnings {}}))
(deftest process-from-node-tests
  (let [output (process-from-node (:csv-input5293 test-molds/test-nodes1) test-molds/test-nodes1 state)]
    (is (= test-tables/tinytable1 output)
        "Process a single input node"))
  (let [output (process-from-node (:drop-columns123 test-molds/test-nodes1) test-molds/test-nodes1 state)]
    (is (= tinytable1-namedropped output)
        "Process two nodes"))
  (let [output (process-from-node (:csv-output1892 test-molds/test-nodes1) test-molds/test-nodes1 state)]
    (is (= nil output)
        "Process three nodes, ending in output. Shouldn't return anything")
    ; check that the file is written as expected
    (is (= (slurp  "test/molder/test/data/smallset-namedropped.csv") (slurp  "test/molder/test/data/smallset-out.csv"))
        "Check that the last output node is properly run"))
    (io/delete-file (io/file "test/molder/test/data/smallset-out.csv")))

(def expected-state
  { :data
    { :csv-input1
        { :csv-output1 test-tables/tinytable2 }
      :identity222
        { :csv-output1892 tinytable1-namedropped }
      :drop-columns123
        { :identity222 tinytable1-namedropped }
      :csv-input5293
        { :drop-columns123 test-tables/tinytable1 }}
    :errors [],
    :warnings []})

(deftest process-mold-tests
  ; This mold has two outputs - both to .csv files
  ; first check that the state has been properly set
  (let [state (process-mold test-molds/test-nodes2)]
    (is (= expected-state state)
        "Check that the state (ie. data flowing between nodes) has been saved"))

  ; check that the files are written as expected
  (is (= (slurp  "test/molder/test/data/smallset-namedropped.csv") (slurp  "test/molder/test/data/smallset-out.csv"))
      "Check that first csv output triggered properly")
  (io/delete-file (io/file "test/molder/test/data/smallset-out.csv"))

  (is (= (slurp  "test/molder/test/data/smallset2.csv") (slurp  "test/molder/test/data/smallset2-out.csv"))
      "Check that second csv output triggered properly")
  (io/delete-file (io/file "test/molder/test/data/smallset2-out.csv")))
