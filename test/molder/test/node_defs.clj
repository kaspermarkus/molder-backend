(ns molder.test.node-defs
  (:use [clojure.test]
        [molder.node-defs]
        [slingshot.slingshot :only [throw+ try+]]))

; test :default multi-method
(def bogus-node { :type "bogus",
                  :id "csv-input5293"
                  :name "Adresses CSV"
                  :inputs nil
                  :outputs [ :identity9101 ]
                  :fields
                    { :filename "test/molder/test/data/csv/smallset.csv"
                      :header true
                      :separator \;
                  }})


(deftest run-node-default-test
  (let [thrown (atom false)]
    (try+
      (run-node bogus-node)
      (catch [:type :invalid-mold] {:keys [severity description]}
        (is (= severity :error) "Error level correct")
        (is (= description "Retrieved a node-type that does not exist in the system")
            "We're getting the correct error message")
        (swap! thrown not)))
    (is (= @thrown true) "An error message should be thrown")))

(deftest node-metadata-test
  (let [thrown (atom false)]
    (try+
      (node-metadata bogus-node)
      (catch [:type :invalid-mold] {:keys [severity description]}
        (is (= severity :error) "Error level correct")
        (is (= description "Retrieved a node-type that does not have any metadata for it.")
            "We're getting the correct error message")
        (swap! thrown not)))
    (is (= @thrown true) "An error message should be thrown")))
