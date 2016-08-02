# Adding new node types to the system #

The system supports adding new nodes via multimethods for the running and getting metadata of the node. There are three parts to adding a new node to the system:
1. Add multimethod definitions pointing to your implementation of the running and metadata retrieval
2. Add implementation of the actual node functionality
3. Add tests for the new node

Details on these steps are given below.

## STEP 1: Add multimethod definitions ##

In the node inclusion file (`/src/molder/node_defs.clj`), add two multimethod entries, one for `run-node` and one for `node-metadata`. For example:

```
(defmethod run-node :csv-input [node] (csv-input/csv-input node))
(defmethod node-metadata :csv-input [_] csv-input/metadata)
```

where `:csv-input` should be replaced by your node type, `csv-input/csv-input` should be replaced by a reference to your implementation of the run-node function and `csv-input/metadata` should be replaced by a reference to the metadata for the node type.

Generally you should put your implementation in a separate file and require it, like so (replacing the `input` and `csv-input` parts with proper values`):

```
(:require [molder.input.csv-input :as csv-input]))
```

## STEP 2 AND 3: Implementation and test of the node functionality ##

The details of this is up to you - but:
* Generally put the implementation in its own file
* Make sure you have tests for it
* Your metadata and implementation should adhere to the definitions described in [API documentation (API.md)](API.md).