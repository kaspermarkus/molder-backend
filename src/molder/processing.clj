(ns molder.processing
  (:use [molder.node-defs])
  (:use [slingshot.slingshot :only [throw+ try+]])
  (:require [molder.error-handling :as error-handling]
            [molder.utils :as utils]))

;
(defn external-output-node?
  "Returns true if the node has only inputs (ie. the node outputs to something like a file)"
  [node]
  (= 0 (:out-points (node-metadata (:type node)))))

;
(defn external-input-node?
  "Returns true if the node has only outputs (ie. the node reads from other source, like a file)"
  [node]
  (= 0 (:in-points (node-metadata (:type node)))))

(defn is-missing-output-node?
  "Returns true if there is no node(s) attached as output to the given nodes"
  [node]
  (let [outpoints (:out-points (node-metadata (:type node)))
        connections (count (:outputs node))]
    (not (= outpoints connections))))

(defn filter-missing-output-nodes
  "Given a list of nodes, only keep those for which there is no node(s) attached as output"
  [nodes]
  (filter is-missing-output-node? (vals nodes)))

(defn filter-external-output-nodes
  "Returns list of nodes that are output nodes (ie. have out-points: 0)"
  [nodes]
  (filter external-output-node? (vals nodes)))

(defn filter-nodes-to-try
  "Filter all nodes to process from. They qualify for this if they're either external output
   nodes or are nodes that are supposed to have outputs but no current output connections.
   The reason for filtering these is because if we want sample data for all connections/nodes, we
   need to process from all these nodes."
  [nodes]
  (filter
    (fn [node] (or (is-missing-output-node? node) (external-output-node? node)))
    (vals nodes)))

(defn get-node-from-id
  "From a list of nodes, retrieve the node with the given id"
  [id nodes]
  (get nodes (keyword id)))

(defn get-input-nodes
  "Retrieve all the input nodes, based on a list of :inputs"
  [node nodes]
  (map
    (fn [id] (get-node-from-id id nodes))
    (:inputs node)))

(defn limit-data
  "Given a data entry (including headers), does a take on the data section limiting the results to
   the limit given in parameter"
  [lines table]
  (assoc-in table [ :data ] (take lines (:data table))))

(defn add-single-sample-entry-to-state
  "Adds a single sample entry to the data of the state from the fromId to the toId.
   This is used by add-data-to-state and should generally not be called directly"
  [sample from-id to-id state]
  (swap! state assoc-in [ :data (keyword from-id) (keyword to-id)] sample))

(defn add-data-to-state
  "Adds the given data to the state as output from the given node.
   If a limit to provided, the number of entries provided is limited to that.
   If the node doesn't have any outputs, it'll be stored as sample data to :no-connection"
  ([data node state]
      (doall (map (fn [to-node] (add-single-sample-entry-to-state data (:id node) to-node state))
                  (:outputs node))))
  ([data node state limit]
      (doall (map (fn [to-node] (add-single-sample-entry-to-state (limit-data limit data) (:id node) to-node state))
                  (:outputs node)))))

(defn add-unbounded-data-to-state[data node state ]
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
      ; Also run all nodes that do not actaully output anywhere, just to get the
      ; error/warning messages from them
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

; "Do a trial run of the mold from the given node, recoding errors and data to the given
;  state and limiting the amount of data recorded to `limit` entries."
(defmulti try-from-node
  (fn [state node mold limit]
    (if (external-input-node? node)
      :external-input-node
      (if (external-output-node? node)
        :external-output-node
        :regular-node))))

; if it's an external input node, dont pass any input to it
(defmethod try-from-node :external-input-node [state node mold limit]
  (do
      (validate-node node nil state) ; first validate node
      (let [data (run-node node)]
        (add-data-to-state data node state limit)
        data))) ; return data

; if it's an external output node, only validate node - dont run it. Instead take `limit`
; entries to force all previous parts of the pipe to be processed
(defmethod try-from-node :external-output-node [state node mold limit]
  (let [input-nodes (get-input-nodes node mold)
        resolved-inputs (map (fn [in-node] (limit-data limit (try-from-node state in-node mold limit)))
                             input-nodes)]
    (validate-node node resolved-inputs state)
    (first resolved-inputs))) ; since we dont want to write to ext. output, return data w/o running node

; if it's just a regular (single input, single output) node, run it and record `limit` entries
; onto the state. Return the output
(defmethod try-from-node :regular-node [state node mold limit]
  (let [input-nodes (get-input-nodes node mold)
        resolved-inputs (map (fn [in-node] (try-from-node state in-node mold limit)) input-nodes)]
    (validate-node node resolved-inputs state)
    ; run current node with the resolved input data and store sample data in state
    (let [data (run-node node resolved-inputs)]
      (add-data-to-state (limit-data limit data) node state)
      data))) ; return data

(defn try-mold [mold limit]
  (let [state (atom { :data {} :errors [] :warnings [] })]
    (try+
      ; First run all the nodes with external output
      (let [ext-output-nodes (filter-external-output-nodes mold)]
        (doall
          (map (fn [node]
                  (let [data (try-from-node state node mold limit)]
                    (add-unbounded-data-to-state (limit-data limit data) node state)))
                ext-output-nodes)))
      ; Also run all nodes that do not actaully output anywhere, just to get the
      ; error/warning messages from them
      (let [missing-output-nodes (filter-missing-output-nodes mold)]
        (doall
          (map (fn [node]
                  (let [data (try-from-node state node mold limit)]
                       (add-unbounded-data-to-state (limit-data limit data) node state)))
                missing-output-nodes)))
      @state
      (catch [:severity :error] err
        (do
          (println "ERRORRRRRR " err)
          (utils/add-error-to-state err state)
          @state))
      (catch Exception e
        (do
          (println "EROROROOROROROROORORORORORO: " e)
          @state)))))












































