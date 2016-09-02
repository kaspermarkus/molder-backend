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
              :default ","
              :name "Separator",
              :tooltip "The character separating the entries in the CSV file" }}}}

### ACTUAL NODE: ###
{
    :type "csv-in",
    :id "csv-in14",
    :inputs nil
    :outputs [ "drop-columns123" ]
    :fields 
      { :filename "/tmp/deleme.csv"
        :header true
        :separator ";" },
    :name "Adresses CSV"
}

### TABLE DATA ###
{
  :columns {
    :myheader1
      { :type "char" ;not required
        :index 0 }
    :myheader2 
      { :index 1 }
  }
  :data [( \k "Kasper Markus" )
         ( \l "Lasse Markus") ] }


### MOLD (i.e. map of nodes): ###
{ :csv-input5293 {
    :type "csv-input",
    :id "csv-input5293"
    :name "Adresses CSV"
    :inputs nil
    :outputs [ "drop-columns123" ]
    :fields
      { :filename "test/molder/test/data/smallset.csv"
        :header true
        :separator ";"
    }}
    (....)
  :csv-output1892 {
    :type "csv-output"
    :id "csv-output1892"
    :name "Modified addresses"
    :inputs [ "identity222" ]
    :outputs nil
    :fields {
        :filename "test/molder/test/data/smallset-out.csv"
        :header true
        :separator ";"
    }}

### API: ###

##### Load project:
URL: GET /load-mold?filename=<filename>
Returns: A mold (see above)

##### Save project:
URL: POST /save-mold?filename=<filename>
body: A mold (see above)
Returns: Status code

##### Load full application information:
URL: /node-metadata
Returns: a map of node-metadata, keyed by node-type

##### Run the mold/pipe for realz
URL: /run <mold>

Does a full run of the pipes

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
    :warnings [{}]