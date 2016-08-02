(ns molder.test.processing
  (:use [clojure.test]
        [molder.processing]
        [molder.node-defs])
  (:require
    [clojure.java.io :as io]
    [molder.test.data.molds :as test-molds]))

(deftest external-output-node?-tests
  (is (= false (external-output-node? (:csv-input5293 test-molds/test-nodes1)))
      "Check if input node evaluates to external output node")
  (is (= false (external-output-node? (:identity9101 test-molds/test-nodes1)))
      "Check if standard node evaluates to external output node")
  (is (= true (external-output-node? (:csv-output1892 test-molds/test-nodes1)))
      "Check if input node evaluates to external output node"))

(deftest external-input-node?-tests
  (is (= true (external-input-node? (:csv-input5293 test-molds/test-nodes1)))
      "Check if input node evaluates to external input node")
  (is (= false (external-input-node? (:identity9101 test-molds/test-nodes1)))
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

(deftest get-node-from-id-test
  (is (= (:csv-output1892 test-molds/test-nodes1)
         (get-node-from-id "csv-output1892" test-molds/test-nodes1))))

(deftest get-input-nodes-test
  (is (= (list (:identity9101 test-molds/test-nodes1))
         (get-input-nodes (:csv-output1892 test-molds/test-nodes1) test-molds/test-nodes1))))

(def test-input-csv-expected1
    '({:Name "Kasper" :Age "31" :Country "Switzerland"}
      {:Name "Kevin" :Age "31" :Country "Denmark"}
      {:Name "Santa Clause" :Age "800" :Country "North Pole"}))

(def test-input-csv-expected2
    '({:header1 "Kasper" :header2 "31" :header3 "Switzerland"}
      {:header1 "Kevin" :header2 "31" :header3 "Denmark"}
      {:header1 "Santa Clause" :header2 "800" :header3 "North Pole"},
      {:header1 "Mike Smith", :header2 "", :header3 ""}))

(def state (atom { :data {} :errors {} :warnings {}}))
(deftest process-from-node-tests
  (let [output (process-from-node (:csv-input5293 test-molds/test-nodes1) test-molds/test-nodes1 state)]
    (is (= test-input-csv-expected1 output)
        "Process a single input node"))
  (let [output (process-from-node (:identity9101 test-molds/test-nodes1) test-molds/test-nodes1 state)]
    (is (= test-input-csv-expected1 output)
        "Process two nodes"))
  (let [output (process-from-node (:csv-output1892 test-molds/test-nodes1) test-molds/test-nodes1 state)]
    (is (= nil output)
        "Process three nodes, ending in output. Shouldn't return anything")
    ; check that the file is written as expected
    (is (= (slurp  "test/molder/test/data/smallset.csv") (slurp  "test/molder/test/data/smallset-out.csv"))
        "Check that the last output node is properly run"))
    (io/delete-file (io/file "test/molder/test/data/smallset-out.csv")))

(def table1
  '({:Name "Kasper", :Age "31", :Country "Switzerland"}
    {:Name "Kevin", :Age "31", :Country "Denmark"}
    {:Name "Santa Clause", :Age "800", :Country "North Pole"}))

(def table2
  '({:Region "East", :UnitCost "1.99"}
    {:Region "North-Central", :UnitCost "19.99"}
    {:Region "Mid-Central", :UnitCost "4.99"}
    {:Region "Central", :UnitCost "19.99"}
    {:Region "West", :UnitCost "2.99"}))

(def expected-state
  { :data
    { :csv-input1
        { :csv-output1 table2 }
      :identity222
        { :csv-output1892 table1 }
      :identity9101
        { :identity222 table1 }
      :csv-input5293
        { :identity9101 table1 }}
    :errors {},
    :warnings {}})

(deftest process-mold-tests
  ; This mold has two outputs - both to .csv files
  ; first check that the state has been properly set
  (let [state (process-mold test-molds/test-nodes2)]
    (is (= expected-state state)
        "Check that the state (ie. data flowing between nodes) has been saved"))

  ; check that the files are written as expected
  (is (= (slurp  "test/molder/test/data/smallset.csv") (slurp  "test/molder/test/data/smallset-out.csv"))
      "Check that first csv output triggered properly")
  (io/delete-file (io/file "test/molder/test/data/smallset-out.csv"))

  (is (= (slurp  "test/molder/test/data/smallset2.csv") (slurp  "test/molder/test/data/smallset2-out.csv"))
      "Check that second csv output triggered properly")
  (io/delete-file (io/file "test/molder/test/data/smallset2-out.csv")))
