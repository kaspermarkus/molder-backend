(ns molder.node-defs
  (:require
    [molder.nodes.input.csv-input :as csv-input]
    [molder.nodes.output.csv-output :as csv-output]
    [molder.nodes.identity :as ident-node]
    [molder.nodes.drop-columns :as drop-columns])
  (:use [slingshot.slingshot :only [throw+ try+]]))

(defn node-type
  "Returns the :node-type for multimethod resolution"
  [x & xs] (keyword (get x :type)))

(def available-nodetypes
  '( :csv-input :csv-output :identity :drop-columns))

(defmulti run-node node-type)
(defmulti node-metadata (fn [type] (keyword type)))
(defmulti validate-node node-type)

(defmethod run-node :csv-input [node] (csv-input/csv-input node))
(defmethod node-metadata :csv-input [_] csv-input/metadata)
(defmethod validate-node :csv-input [node _ state] (csv-input/validate node state))

(defmethod run-node :csv-output [node table] (csv-output/csv-output node (first table)))
(defmethod node-metadata :csv-output [_] csv-output/metadata)

(defmethod run-node :identity [node table] (ident-node/run-node node (first table)))
(defmethod node-metadata :identity [_] ident-node/metadata)

(defmethod run-node :drop-columns [node table] (drop-columns/run-node node (first table)))
(defmethod node-metadata :drop-columns [_] drop-columns/metadata)
(defmethod validate-node :drop-columns [node table state] (drop-columns/validate node (first table) state))
; (defmethod validate-node :drop-columns [node _ state] (drop-columns/validate node state))

; Default defmethod for `run-node`. If this gets called, it means that there is no method for the node-type
; which results in an error.
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

; Default defmethod for `node-metadata`. If this gets called, it means that there is no method for the node-type
; which results in an error.
(defmethod node-metadata :default [node]
  (throw+
    { :severity :error
      :node (:id node)
      :type :invalid-mold
      :description "Retrieved a node-type that does not have any metadata for it."
      :details (str "node-metadata multimethod caught default because no definition of a method for "
                    "a node of type \"" (:type node) "\" was provided. "
                    "The node in question has the ID: \"" (:id node) \"", and the full node content is: "
                    node)}))

(def all-node-metadata
  (zipmap
    available-nodetypes
    (map
      (fn [type] (node-metadata type))
      available-nodetypes)))
