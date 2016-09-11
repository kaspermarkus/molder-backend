(ns molder.test.processing
  (:use [clojure.test]
        [molder.processing]
        [molder.node-defs])
  (:require
    [clojure.java.io :as io]
    [molder.test.data.molds :as test-molds]
    [molder.test.data.simpletables :as test-tables]
    [molder.nodes.drop-columns :as drop-columns]
    [molder.test-utils :as test-utils]))

(def tinytable1-namedropped (drop-columns/drop-columns-impl [ "Name" ] test-tables/tinytable1))

(deftest external-output-node?-tests
  (is (= false (external-output-node? (:csv-input5293 test-molds/test-mold-three-nodes)))
      "Check if input node evaluates to external output node")
  (is (= false (external-output-node? (:drop-columns123 test-molds/test-mold-three-nodes)))
      "Check if standard node evaluates to external output node")
  (is (= true (external-output-node? (:csv-output1892 test-molds/test-mold-three-nodes)))
      "Check if input node evaluates to external output node"))

(deftest is-missing-output-node?-tests
  (is (= false (is-missing-output-node? (:csv-input5293 test-molds/test-mold-three-nodes)))
      "Check if input node evaluates to false if it does have an output")
  (is (= false (is-missing-output-node? (:drop-columns123 test-molds/test-mold-three-nodes)))
      "Check if standard node evaluates to false if it does have an output")
  (is (= false (is-missing-output-node? (:csv-output1892 test-molds/test-mold-three-nodes)))
      "Check if external output node is interpreted correctly")
  (is (= true (is-missing-output-node? (:csv-input5293 test-molds/test-nodes3)))
      "Check if external input node evaluates to true if missing output")
  (is (= true (is-missing-output-node? (:drop-columns123 test-molds/test-nodes4)))
      "Check if transformation node evaluates to true if missing output"))

(deftest external-input-node?-tests
  (is (= true (external-input-node? (:csv-input5293 test-molds/test-mold-three-nodes)))
      "Check if input node evaluates to external input node")
  (is (= false (external-input-node? (:drop-columns123 test-molds/test-mold-three-nodes)))
      "Check if standard node evaluates to external input node")
  (is (= false (external-input-node? (:csv-output1892 test-molds/test-mold-three-nodes)))
      "Check if input node evaluates to external input node"))

(deftest filter-external-output-nodes-tests
  (is (= (list (:csv-output1892 test-molds/test-mold-three-nodes))
         (filter-external-output-nodes test-molds/test-mold-three-nodes))
      "Simple list with just one output")
  (is (= (list (:csv-output1892 test-molds/test-mold-multiple-pipes) (:csv-output1 test-molds/test-mold-multiple-pipes))
         (filter-external-output-nodes test-molds/test-mold-multiple-pipes))
      "List with two outputs"))

(deftest filter-nodes-to-try-tests
  (is (= (list
            (:drop-columns123 test-molds/test-nodes4)
            (:csv-output1 test-molds/test-nodes4))
         (filter-nodes-to-try test-molds/test-nodes4))
      "Filter with one external output and one incomplete"))

(deftest get-node-from-id-test
  (is (= (:csv-output1892 test-molds/test-mold-three-nodes)
         (get-node-from-id "csv-output1892" test-molds/test-mold-three-nodes))))

(deftest get-input-nodes-test
  (is (= (list (:drop-columns123 test-molds/test-mold-three-nodes))
         (get-input-nodes (:csv-output1892 test-molds/test-mold-three-nodes) test-molds/test-mold-three-nodes))))

(def state (atom { :data {} :errors {} :warnings {}}))
(deftest process-from-node-tests
  (let [output (process-from-node (:csv-input5293 test-molds/test-mold-three-nodes) test-molds/test-mold-three-nodes state)]
    (is (= test-tables/tinytable1 output)
        "Process a single input node"))
  (let [output (process-from-node (:drop-columns123 test-molds/test-mold-three-nodes) test-molds/test-mold-three-nodes state)]
    (is (= tinytable1-namedropped output)
        "Process two nodes"))
  (let [output (process-from-node (:csv-output1892 test-molds/test-mold-three-nodes) test-molds/test-mold-three-nodes state)]
    (is (= nil output)
        "Process three nodes, ending in output. Shouldn't return anything")
    ; check that the file is written as expected
    (is (= (slurp  "test/molder/test/data/csv/smallset-namedropped.csv") (slurp  "test/molder/test/data/csv/smallset-out.csv"))
        "Check that the last output node is properly run"))
    (io/delete-file (io/file "test/molder/test/data/csv/smallset-out.csv")))

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
  (let [state (process-mold test-molds/test-mold-multiple-pipes)]
    (is (= expected-state state)
        "Check that the state (ie. data flowing between nodes) has been saved"))

  ; check that the files are written as expected
  (is (= (slurp  "test/molder/test/data/csv/smallset-namedropped.csv") (slurp  "test/molder/test/data/csv/smallset-out.csv"))
      "Check that first csv output triggered properly")
  (io/delete-file (io/file "test/molder/test/data/csv/smallset-out.csv"))

  (is (= (slurp  "test/molder/test/data/csv/smallset2.csv") (slurp  "test/molder/test/data/csv/smallset2-out.csv"))
      "Check that second csv output triggered properly")
  (io/delete-file (io/file "test/molder/test/data/csv/smallset2-out.csv")))

; (deftest try-from-node-test
  ; with a mold consisting of only a single input node
  ; max 2 lines to state
  ; (test-utils/clear-state state)
  ; (let [
  ;   mold test-molds/test-mold-single-node
  ;   result-state (try-from-node (:node0 mold) mold 2 state)
  ;   exp-data (limit-data 2 test-tables/tinytable1)
  ;   expected { :errors []
  ;              :warnings []
  ;              :data
  ;                { :node0
  ;                  { :no-connection exp-data }}}]
  ;   (println "result state " @state)
  ;   (println "Expected:    " expected)
  ;   (println "exp-data " exp-data)
  ;   (is (= result-state expected)))
  ; mold with three nodes all in a single pipe. Limit to 1 line
  ; (test-utils/clear-state state)
  ; (let [
  ;   mold test-molds/test-mold-three-nodes
  ;   result-state (try-from-node (:csv-output1892 mold) mold 1 state)
  ;   exp-data (limit-data 2 test-tables/tinytable1)
  ;   expected { :errors []
  ;              :warnings []
  ;              :data
  ;                { :csv-output1892
  ;                  { :no-connection exp-data }}}]
  ;   (println "result state " @state)
  ;   (println "Expected:    " expected)
  ;   (println "exp-data " exp-data)
  ;   (is (= result-state expected))))

(deftest try-mold-tests
  ; Test mold with a single node in it and no external endpoints
  (let [result-state (try-mold test-molds/test-mold-single-node 2)
        expected { :errors []
                   :warnings []
                   :data
                     { :node0
                       { :no-connection (limit-data 2 test-tables/tinytable1) }}}]
    (is (= expected result-state)))
  ; Test mold with two nodes and no external output
  (let [result-state (try-mold test-molds/test-mold-two-nodes-no-ext-output 2)
        expected { :errors []
                   :warnings []
                   :data
                     { :csv-input5293
                       { :drop-columns123 (limit-data 2 test-tables/tinytable1) }
                       :drop-columns123
                       { :no-connection (limit-data 2 tinytable1-namedropped )}}}]
    (is (= expected result-state)))
  ; Test mold with three nodes incl. external endpoint
  (let [result-state (try-mold test-molds/test-mold-three-nodes 2)
        expected { :errors []
                   :warnings []
                   :data
                     { :csv-input5293
                       { :drop-columns123 (limit-data 2 test-tables/tinytable1) }
                       :drop-columns123
                       { :csv-output1892 (limit-data 2 tinytable1-namedropped )}
                       :csv-output1892
                       { :no-connection (limit-data 2 tinytable1-namedropped )}}}]
    (is (= expected result-state))
    (is (not (.exists (io/file "test/molder/test/data/csv/smallset-out.csv")))
      "The output node shouldn't actually output anything"))
  ; Test mold with multiple pipes
  (let [result-state (try-mold test-molds/test-mold-multiple-pipes 2)
        expected { :errors []
                   :warnings []
                   :data
                     { :csv-input5293
                       { :drop-columns123 (limit-data 2 test-tables/tinytable1) }
                       :drop-columns123
                       { :identity222 (limit-data 2 tinytable1-namedropped )}
                       :identity222
                       { :csv-output1892 (limit-data 2 tinytable1-namedropped )}
                       :csv-output1892
                       { :no-connection (limit-data 2 tinytable1-namedropped )}

                       :csv-input1
                       { :csv-output1 (limit-data 2 test-tables/tinytable2) }
                       :csv-output1
                       { :no-connection (limit-data 2 test-tables/tinytable2 )}}}]
    (is (= expected result-state))
    (is (not (.exists (io/file "test/molder/test/data/csv/smallset-out.csv")))
      "The output node shouldn't actually output smallset-out")
    (is (not (.exists (io/file "test/molder/test/data/csv/smallset2-out.csv")))
      "The output node shouldn't actually output smallset2-out")))






































