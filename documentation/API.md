## Data and APIs

### NODETYPE METADATA ###
:csv-in {
    :end-points-in 1
    :end-points-out 0
    :typeName "CSV Input"
    :description "blablablabla"
    :fields 
        { :filename 
            { :type "file"
              :required true
              :default ""
              :name "Filename",
              :tooltip: "The full filename of the .CSV file to read from" }
          :header
            { :type "boolean"
              :required true
              :default true
              :name "Has header"
              :tooltip: "Should be true (marked) if the first line of the CSV file is the column headers/titles" }
          :separator
            { :type "char"
              :required true
              :default ';'
              :name "Separator",
              :tooltip "The character separating the entries in the CSV file" }}}}

### NODE: ###
{
    :type "csv-in",
    :id "csv-in14"
    :fields 
      { :filename "/tmp/deleme.csv"
        :header true
        :separator ";" },
    :name "Adresses CSV"
}

#### EDGES: ####
{ :csv-in14 [ :mynode1 :mynode2 :mynodeN ]
  :other_node18 [ :somenode1 :somenode2 ]}

### PIPES: ###
( {node1 (input+)} {node2}(1*input 1*output) ... {node_n-1}(1*input 1*output) {node_n (output+)} )

### API: ###

##### Load project:
URL: /load filename.clj
Returns: { nodes: <nodes>, edges: <edges> }

##### Save project:
URL: /save <project.clj> <nodes> <edges>
Returns: Status code and the same output as /try

##### Load full application information:
URL: /node_metadata
Returns: a map of 'nodetypes', keyed by their ids

##### Run the mold/pipe for realz
URL: /run <nodes> <edges>

Does a full run of the pipes

Returns: Status code, result

##### Try running the mold, report status
URL: /try <nodes> <edges>

Tries running the pipe and returns a information about the data that is run through the mold.

Returns:
  { :data
    { :csv-input1
        { :csv-output1 [...table...] }
      :identity222
        { :csv-output1892 [...table...] }
      :identity9101
        { :identity222 [...table...] }
      :csv-input5293
        { :identity9101 [...table...] }}
    :errors [{
      :severity :error
      :node <node-id>
      :type :invalid-mold
      :description "..."
      :details "..."
    }],
    :warnings {}