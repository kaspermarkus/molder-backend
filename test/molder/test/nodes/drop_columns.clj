(ns molder.test.nodes.drop-columns
  (:use [clojure.test]
        [molder.core]
        [molder.node-defs]
        [slingshot.slingshot :only [throw+ try+]])
  (:require [molder.nodes.drop-columns :as drop-columns]
    [molder.error-handling :as error-handling]
    [molder.test.data.simpletables :as test-tables]))

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
  (let [thrown (atom false)]
    (try+
      (drop-columns/validate nodedef4 test-tables/tinytable1)
      (catch [:severity :warning] { :keys [ type description ]}
        (is (= :parameter-error type))
        (is (= description "Drop Column(s) node retrieved an empty list of column names to drop")
            "We're getting the correct error message")
        (swap! thrown not)))
    (is (= true @thrown) "An error message should be thrown"))

  (let [thrown (atom false)]
    (try+
      (drop-columns/validate nodedef2 test-tables/tinytable1)
      (catch [:severity :warning] { :keys [ type description ]}
        (is (= :parameter-error type))
        (is (= description "Drop Column(s) found a column name to drop that wasn't present in the input table")
            "We're getting the correct error message")
        (swap! thrown not)))
    (is (= true @thrown) "An error message should be thrown")))

; test multimethod
(deftest test-validate-multimethod
  (let [thrown (atom false)]
    (try+
      (validate-node nodedef4 test-tables/tinytable1)
      (catch [:severity :warning] { :keys [ type description ]}
        (is (= :parameter-error type))
        (swap! thrown not)))
    (is (= true @thrown) "An error message should be thrown")))

; Check metadata multimethod is properly overwritten
(deftest test-metadata-multimethod
  (is (= (node-metadata :drop-columns) drop-columns/metadata)))

