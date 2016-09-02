# Adding new node types to the system #

The system supports adding new nodes via multimethods for the running and getting metadata of the node. There are three parts to adding a new node to the system:
1. Announce the new node type in the system via multimethod definitions for processing and retrieving metadata.
2. Add implementation of the actual node functionality
3. Add tests for the new node

Details on these steps are given below.

## STEP 1: Announce your node type in the system ##


In the node inclusion file (`/src/molder/node_defs.clj`), first add your node type to the list of available node-types kept in the `available-nodetypes` variable:

```
(def available-nodetypes
  '( :csv-input :csv-output :identity :drop-columns :my-node-type))
```

add the multimethod entries, one for `run-node` and one for `node-metadata`. For example:

```
(defmethod run-node :my-node-type [node] (my-nodes/my-node-type node))
(defmethod node-metadata :my-node-type [_] my-nodes/my-metadata)
```

where `:my-node-type` should be replaced by your node type, `my-nodes/my-node-type` should be replaced by a reference to your implementation of the run-node function and `my-nodes/my-metadata` should be replaced by a reference to the metadata for the node type.

It is also encouraged, though optional to provide a validation function for your node-type. Given a node and a table, it should check that the properties set in the node are valid and that they apply to the given table. If not, the appropriate errors/warnings should be trown. The validation function is implemented like the `run-node` and `node-metadata` methods, via the `defmethod` overwrite:

```
(defmethod validate-node :my-node-type [node table] (my-node-type/validate node table)
```

Generally you should put your implementation in a separate file and require it, like so (replacing the `my-nodes` and `my-node-type` parts with proper values`):

```
(:require [molder.my-nodes.my-node-type :as my-node-type]))
```

## STEP 2 AND 3: Implementation and test of the node functionality ##

The details of this is up to you - but:
* Generally put the implementation in its own file
* Make sure you have tests for it
* Your metadata and implementation should adhere to the definitions described in [API documentation (API.md)](API.md).