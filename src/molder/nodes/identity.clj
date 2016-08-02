(ns molder.nodes.identity)

(require '[clojure.data.csv :as csv]
         '[clojure.java.io :as io])

(defn run-node [node table] table)

(def metadata
  { :in-points 1
    :out-points 1
    :type-name "Identity"
    :type :identity
    :description "Identity transform. I.e. outputs the input table"
    :fields {}})