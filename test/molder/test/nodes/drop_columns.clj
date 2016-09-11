(ns molder.test.nodes.drop-columns
  (:use [clojure.test]
        [molder.core]
        [molder.node-defs]
        [slingshot.slingshot :only [throw+ try+]])
  (:require [molder.nodes.drop-columns :as drop-columns]
    [molder.error-handling :as error-handling]
    [molder.test.data.simpletables :as test-tables]
    [molder.test-utils :as test-utils]))

(def expected-table1
  { :columns
     { :Age { :index 0 }
       :Country { :index 1 }}
    :data
      '( [ "31" "Switzerland" ]
         [ "31" "Denmark" ]
         [ "800" "North Pole"]) })

(def expected-table2
  { :columns
     { :Age { :index 0 }}
    :data
      '( [ "31" ]
         [ "31" ]
         [ "800"]) })


;Test input-csv function
(deftest test-drop-columns-impl
    (is (= (drop-columns/drop-columns-impl [ :Name ] test-tables/tinytable1) expected-table1)
        "dropping single column")
    (is (= (drop-columns/drop-columns-impl [ :Name :Country ] test-tables/tinytable1) expected-table2)
        "dropping multiple columns")
    (is (= (drop-columns/drop-columns-impl [ ] test-tables/tinytable1) test-tables/tinytable1)
        "dropping empty set of columns")
    (is (= (drop-columns/drop-columns-impl [ :Name :bogus ] test-tables/tinytable1) expected-table1)
        "dropping non-existing columns")
    (is (= (drop-columns/drop-columns-impl [ "Name" "Country" ] test-tables/tinytable1) expected-table2)
        "Column names as strings (should be converted to keywords)"))

(def nodedef1
    { :type :drop-columns,
      :id "drop-columns24"
      :fields { :column-names [ :Name :Country ]}
      :name "Cleaned table"})

(def nodedef2
    { :type :drop-columns,
      :id "drop-columns24"
      :fields { :column-names [ :Name :Bogus ]}
      :name "Cleaned table"})

(def nodedef3
    { :type :drop-columns,
      :id "drop-columns24"
      :fields { :column-names [ "Name" "Country" ]}
      :name "Cleaned table"})

(def nodedef4
    { :type :drop-columns,
      :id "drop-columns24"
      :fields { :column-names [ ]}
      :name "Cleaned table"})

;Test multi-method overwrite and parameters
(deftest test-run-node-multimethod
    (is (= expected-table2 (run-node nodedef1 (list test-tables/tinytable1)))
        "Run node successfully drops columns")
    (is (= expected-table2 (run-node nodedef3 (list test-tables/tinytable1)))
        "Run node successfully drops columns - non keyword column names")
    (is (= expected-table1 (run-node nodedef2 (list test-tables/tinytable1)))
        "Run-node successfully drops columns with one non-existing column name"))

; Test validation function:
(deftest test-validate
  (let [state (atom test-utils/state-template)]
    ; (println "INPUT: " test-tables/tinytable1)
    ; no errors/warnings
    (validate-node nodedef1 (list test-tables/tinytable1) state)
    (is (= @state test-utils/state-template) "Validation with no errors nor warnings")
    ; no input
    (validate-node nodedef1 nil state)
    (is (= (:errors @state)
           [{ :type :input-error
              :description "drop-columns24 did not retrieve any input data"
              :node "drop-columns24"}])
          "Error on no input")
    ; ; empty list of column names
    (test-utils/clear-state state)
    (validate-node nodedef4 (list test-tables/tinytable1) state)
    (is (= @state (assoc-in test-utils/state-template [ :warnings 0 ]
                      { :type :parameter-error
                        :field :column-names
                        :node "drop-columns24"
                        :description "Drop Column(s) node retrieved an empty list of column names to drop"}))
          "Error on empty list of column names")
    ; ; test for non-existing column names to drop
    (test-utils/clear-state state)
    (validate-node nodedef2 (list test-tables/tinytable1) state)
    (is (= (:warnings @state)
           [{ :type :parameter-error
              :field :column-names
              :column-name :Bogus
              :node "drop-columns24"
              :description "Drop Column(s) found a column name to drop that wasn't present in the input table"}])
          "Error on empty list of column names")))

; Check metadata multimethod is properly overwritten
(deftest test-metadata-multimethod
  (is (= (node-metadata :drop-columns) drop-columns/metadata)))

