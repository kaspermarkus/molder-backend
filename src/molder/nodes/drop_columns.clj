(ns molder.nodes.drop-columns
  (:require
    [molder.error-handling :as errors]
    [molder.utils :as utils]))

(defn validate [node table state ]
  (println "VALIDATING DROP COLUMNS for node " node)
  (let [ column-names (get-in node [ :fields :column-names ]) ]
    (println "Checking table " table); check for empty list
    (if (= table nil)
      (errors/add-error state { :type :input-error
                       :description "Drop Column(s) did not retrieve any input data"
                       :node (:id node) }))
    (println "checking column names " column-names)
    (if (= 0 (count column-names))
      (errors/add-warning state { :type :parameter-error
                       :field :column-names
                       :description "Drop Column(s) node retrieved an empty list of column names to drop"
                       :node (:id node) }))
    ; Check for columns to drop that arent in the table
    (let [ks (:columns table)]
      (println "KS: " ks " from TABLE: " table)
      (doall
        (map (fn [column-name]
                (do
                  (if (not (contains? ks (keyword column-name)))
                    (errors/add-warning state { :type :parameter-error
                         :field :column-names
                         :column-name column-name
                         :description "Drop Column(s) found a column name to drop that wasn't present in the input table"
                         :node (:id node) }))
                  (println "Checked whether " ks " contained " column-name)))
              column-names)))))

(defn drop-index [ v cindex ] ; TODO test for this
  (vec (concat (subvec v 0 cindex) (subvec v (inc cindex)))))

(defn drop-single-column [ cindex table ]
  (if (nil? cindex)
    table ; return unmodified table
    (let [fixed-data (map (fn [row] (drop-index row cindex)) (:data table))
          columns-as-list (utils/headers-to-list (:columns table))
          fixed-columns (utils/headers-from-list (drop-index columns-as-list cindex))]
      { :columns fixed-columns :data fixed-data})))

; TODO: Throw warnings: one for each entry in the column names to drop that is not in the table
(defn drop-columns-impl
  "Takes [keys] and a table and drops the columns that has the names of the keys"
  [ column-names table ]
  (reduce-kv
    (fn [ outie k v ] (drop-single-column (utils/index-from-column-name v outie) outie))
    table
    column-names))

(defn run-node [node table]
  (let [fields (:fields node)
        column-names (:column-names fields)]
    (drop-columns-impl column-names table)))

(def metadata
  { :in-points 1
    :out-points 1
    :type-name "Drop Column(s)"
    :type "drop-columns"
    :description "Removes the given columns from the input table"
    :fields
      { :column-names
        { :type "array"
          :sub-type "column-name"
          :required true
          :default []
          :name "Column Names"
          :tooltip "The columns to remove from the table" }}})