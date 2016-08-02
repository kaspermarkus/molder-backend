(ns molder.processing
  (:use [molder.node-defs]))

(defmacro is-lazy? [x] `(is (instance? clojure.lang.LazySeq ~x)))

; Returns true if the node has only inputs (ie. the node outputs to something like a file)
(defn external-output-node? [node]
    (= 0 (:out-points (node-metadata (:type node)))))

; Returns true if the node has only outputs (ie. the node reads from other source, like a file)
(defn external-input-node? [node]
    (= 0 (:in-points (node-metadata (:type node)))))

; Returns list of nodes that are output nodes (ie. have out-points: 0)
(defn filter-external-output-nodes [nodes]
  (filter external-output-node? (vals nodes)))

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


; Get the actual data from the node. If it has inputs from other nodes, these will
; be resolved as well.
(defn process-from-node [ node nodes state ]
  (if (external-input-node? node)
    (let [data (run-node node)]
      (add-data-to-state data node state)
      data)
    (let [input-nodes (get-input-nodes node nodes)
          resolved-input-nodes (map
                                  (fn [in-node] (process-from-node in-node nodes state))
                                  input-nodes)
          data (run-node node resolved-input-nodes)]
      (add-data-to-state data node state)
      data)))

; Processes the entire problem/mold. It finds all the external output nodes and processes
; from there via `process-from-node`
(defn process-mold [ nodes ]
  (let
    [filtered-nodes (filter-external-output-nodes nodes)
     state (atom { :data {} :errors {} :warnings {}})]
    (doall (map (fn [node] (process-from-node node nodes state))
                filtered-nodes))
    ; (println "State at finish: " @state)
    @state))
