(ns molder.node-defs
  (:require
    [molder.nodes.input.csv-input :as csv-input]
    [molder.nodes.output.csv-output :as csv-output]
    [molder.nodes.identity :as ident-node])
  (:use [slingshot.slingshot :only [throw+ try+]]))

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

(defmethod run-node :default [node & params]
  (throw+
    { :severity :error
      :node (:id node)
      :type :invalid-mold
      :description "Retrieved a node-type that does not exist in the system"
      :details (str "run-node multimethod caught default because no definition of a method for "
                    "a node of type \"" (:type node) "\" was provided. "
                    "The node in question has the ID: \"" (:id node) \"", and the full node content is: "
                    node)}))
