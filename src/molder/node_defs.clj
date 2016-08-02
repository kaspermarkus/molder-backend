(ns molder.node-defs
  (:require
    [molder.nodes.input.csv-input :as csv-input]
    [molder.nodes.output.csv-output :as csv-output]
    [molder.nodes.identity :as ident-node]))

(defn node-type
  "Returns the :node-type for multimethod resolution"
  [x & xs] (keyword (get x :type)))

(defmulti run-node node-type)
(defmulti node-metadata (fn [type] (keyword type)))

(defmethod run-node :csv-input [node] (csv-input/csv-input node))
(defmethod node-metadata :csv-input [_] csv-input/metadata)

(defmethod run-node :csv-output [node table] (csv-output/csv-output node (first table)))
(defmethod node-metadata :csv-output [_] csv-output/metadata)

(defmethod run-node :identity [node table] (ident-node/run-node node (first table)))
(defmethod node-metadata :identity [_] ident-node/metadata)
