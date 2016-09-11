(ns molder.test.nodes.input.csv-input
  (:use [clojure.test]
        [molder.core]
        [molder.node-defs])
  (:require
        [molder.nodes.input.csv-input :as csv-input]
        [molder.test.data.simpletables :as test-tables]
        [molder.test-utils :as test-utils]))

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
(deftest test-csv-input-impl
  (testing "Testing csv-input-impl function"
    (is (= test-tables/tinytable1
           (csv-input/csv-input-impl "test/molder/test/data/csv/smallset.csv" \; true))
        "read full file with headers and semi-colon separation")
    (is (= { :columns (:columns test-tables/tinytable1) :data (take 2 (:data test-tables/tinytable1)) }
           (csv-input/csv-input-impl "test/molder/test/data/csv/smallset.csv" \; true 2))
        "read only 2 entries from file with headers and semi-colon separation")
    (is (= test-tables/tinytable1
           (csv-input/csv-input-impl "test/molder/test/data/csv/smallset.csv" \; true 200))
        "read more entries from file than exists")
    (is (= test-input-csv-expected2
           (csv-input/csv-input-impl "test/molder/test/data/csv/testdata-noheader.csv" \, false))
        "read file with no headers and comma separation and some empty fields")))

(def nodedef1
    { :type :csv-input,
      :id "csv-in14"
      :fields
      { :filename "test/molder/test/data/csv/smallset.csv"
        :header true
        :separator \; },
      :name "Adresses CSV"})

(def nodedef2
    { :type "csv-input",
      :id "csv-in18"
      :fields
      { :filename "test/molder/test/data/csv/testdata-noheader.csv"
        :header false
        :separator \, },
      :name "Adresses CSV"})

(def nodedef3
    { :type "csv-input",
      :id "csv-in18"
      :fields
      { :filename "test/molder/test/data/csv/testdata-with-tab.csv"
        :header true
        :separator "\\t" },
      :name "Adresses CSV"})

;Test multi-method overwrite and parameters
(deftest test-run-node-multimethod
  (testing "Testing csv-input multimethod"
    (is (= (run-node nodedef1) test-tables/tinytable1)
        "Basic test that it's working")
    (is (= (run-node nodedef2) test-input-csv-expected2)
        "different separator and no headers")
    (is (= (run-node (assoc-in nodedef1 [ :fields :separator ] ";")) test-tables/tinytable1)
        "separator is a string")
    (is (= (run-node nodedef3) test-tables/tinytable1)
        "separator is a tab (\\t) string")))

; Test validation function:
(deftest test-validate
  (let [state (atom test-utils/state-template)]
    ; no errors/warnings
    (validate-node nodedef1 nil state)
    (is (= @state test-utils/state-template) "Validation with no errors nor warnings")
    ; no errors from string separator that is only one char long
    (validate-node (assoc-in nodedef1 [ :fields :separator ] ";") nil state)
    (is (= @state test-utils/state-template) "no errors from string separator that is only one char long")
    ; no errors from string separator with tab
    (validate-node nodedef3 nil state)
    (is (= @state test-utils/state-template) "no errors from string separator with tab")
    ; error if empty string is passed as separator))
    (validate-node (assoc-in nodedef1 [ :fields :separator ] "") nil state)
    (is (= @state (assoc-in test-utils/state-template [ :errors 0 ]
                    { :type :parameter-error
                      :field :separator
                      :node "csv-in14"
                      :description "CSV Input node retrieved an empty separator"
                    }))
        "Error if empty string is passed as separator")
    ; error if separator is a string longer than 1 char
    (test-utils/clear-state state)
    (validate-node (assoc-in nodedef1 [ :fields :separator ] "ai") nil state)
    (is (= @state (assoc-in test-utils/state-template [ :errors 0 ]
                    { :type :parameter-error
                      :field :separator
                      :node "csv-in14"
                      :description "CSV Input node retrieved the separator: 'ai' which is more than one character long"
                    }))
        "non-convertable string is passed as separator")
    ; error if filename is not pointing to a file
    (test-utils/clear-state state)
    (validate-node (assoc-in nodedef1 [ :fields :filename ] "/bogus/file.csv") nil state)
    (is (= @state (assoc-in test-utils/state-template [ :errors 0 ]
                    { :type :parameter-error
                      :field :filename
                      :node "csv-in14"
                      :description "CSV Input node retrieved an invalid filename (/bogus/file.csv) - it does not exist"
                    }))
        "non-convertable string is passed as separator")))