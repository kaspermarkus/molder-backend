(ns molder.test.node-defs
  (:use [clojure.test]
        [molder.node-defs]
        [slingshot.slingshot :only [throw+ try+]]))
  ; (:require
  ;   [clojure.java.io :as io]))

; test :default multi-method
(def bogus-node { :type "bogus",
                  :id "csv-input5293"
                  :name "Adresses CSV"
                  :inputs nil
                  :outputs [ :identity9101 ]
                  :fields
                    { :filename "test/molder/test/data/smallset.csv"
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
