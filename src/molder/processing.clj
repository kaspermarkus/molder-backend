(ns molder.processing
  (:use [molder.node-defs])
  (:use [slingshot.slingshot :only [throw+ try+]])
  (:require [molder.error-handling :as error-handling]
            [molder.utils :as utils]))

; Returns true if the node has only inputs (ie. the node outputs to something like a file)
(defn external-output-node? [node]
    (= 0 (:out-points (node-metadata (:type node)))))

; Returns true if the node has only outputs (ie. the node reads from other source, like a file)
(defn external-input-node? [node]
    (= 0 (:in-points (node-metadata (:type node)))))

(defn is-missing-output-node? [node]
  (let [outpoints (:out-points (node-metadata (:type node)))
        connections (count (:outputs node))]
    (not (= outpoints connections))))

(defn filter-missing-output-nodes [nodes]
  (filter is-missing-output-node? (vals nodes)))

; Returns list of nodes that are output nodes (ie. have out-points: 0)
(defn filter-external-output-nodes [nodes]
  (filter external-output-node? (vals nodes)))

; Filter all nodes to process from. They qualify for this if they're either external output
; nodes or are nodes that are supposed to have outputs but no current output connections.
; The reason for filtering these is because if we want sample data for all connections/nodes, we
; need to process from all these nodes.
(defn filter-nodes-to-try [nodes]
  (filter
    (fn [node] (or (is-missing-output-node? node) (external-output-node? node)))
    (vals nodes)))

; From a list of nodes, retrieve the node with the given id
(defn get-node-from-id [id nodes] (get nodes (keyword id)))

; Retrieve all the input nodes, based on a list of :inputs
(defn get-input-nodes [node nodes]
  (map
    (fn [id] (get-node-from-id id nodes))
    (:inputs node)))

(defn add-data-to-state [ data node state ]
  (doall (map (fn [to-node]
                (swap! state assoc-in [ :data (keyword (:id node)) (keyword to-node)] data))
              (:outputs node))))

(defn add-unbounded-data-to-state [data node state ]
  (swap! state assoc-in [ :data (keyword (:id node)) :no-connection] data))

; Get the actual data from the node. If it has inputs from other nodes, these will
; be resolved as well.
(defn process-from-node [ node nodes state ]
  (try+
    (if (external-input-node? node) ;TODO: mock endpoint if we're just doing a test
      (do
        (validate-node node '() state)
        (let [data (run-node node)] ; if it's an external input node
          (add-data-to-state data node state)
          data))
      (let [input-nodes (get-input-nodes node nodes)
            resolved-input-nodes (map
                                    (fn [in-node] (process-from-node in-node nodes state))
                                    input-nodes)]
            (validate-node node resolved-input-nodes state)
            (let [data (run-node node resolved-input-nodes)]
              (add-data-to-state data node state)
              data)))
    (catch [:severity :warning] warn ;{ :keys [ type description ]}
      (utils/add-warning-to-state warn state))
    (catch [:severity :error] err
      (utils/add-error-to-state err state))
    (catch Exception e (println "EROROROOROROROROORORORORORO: " e))))

; Processes the entire problem/mold. It finds all the external output nodes and processes
; from there via `process-from-node`
(defn process-mold [ nodes ]
  (let [state (atom { :data {} :errors [] :warnings [] })]
    (try+
      ; First run all the nodes with external output - these will automatically be forced to
      ; run through the full input because they will expect to output
      (let [filtered-nodes (filter-external-output-nodes nodes)]
        (doall (map (fn [node] (process-from-node node nodes state))
                    filtered-nodes)))
      (let [secondary-filtered (filter-missing-output-nodes nodes)]
        (doall (map (fn [node]
                      (let [data (process-from-node node nodes state)]
                        (add-unbounded-data-to-state data node state)))
                    secondary-filtered)))
      (println "No unhandable exceptions.. Returning result");
      @state
      (catch [:severity :error] err
        (do
          (utils/add-error-to-state err state)
          @state))
      ; (catch Exception e (println "EROROROROROOROROROROROR")))))
      ; (finally @state))))
)))


