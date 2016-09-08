 {:csv-input5293 {
    :type "csv-input",
    :id "csv-input5293"
    :name "Adresses CSV"
    :inputs nil
    :outputs [ "drop-columns123" ]
    :fields
      { :filename "test/molder/test/data/csv/smallset.csv"
        :header true
        :separator ";"
    }}
  :drop-columns123 {
    :type "drop-columns"
    :id "drop-columns123"
    :name "No name"
    :inputs [ "csv-input5293" ]
    :outputs [ "identity222" ]
    :fields {
      :column-names [ "Name" ]
    }}
  :identity222 {
    :type "identity"
    :id "identity222"
    :name "No mods"
    :inputs [ "drop-columns123" ]
    :outputs [ "csv-output1892" ]
    :fields {}}
  :csv-output1892 {
    :type "csv-output"
    :id "csv-output1892"
    :name "Modified addresses"
    :inputs [ "identity222" ]
    :outputs nil
    :fields {
        :filename "test/molder/test/data/csv/smallset-out.csv"
        :header true
        :separator ";"
    }}


  :csv-input1 {
    :type "csv-input",
    :id "csv-input1"
    :name "my CSV"
    :inputs nil
    :outputs [ "csv-output1" ]
    :fields
      { :filename "test/molder/test/data/csv/smallset2.csv"
        :header true
        :separator ";"
      }}
  :csv-output1 {
    :type "csv-output"
    :id "csv-output1"
    :name "Modified addresses"
    :inputs [ "csv-input1" ]
    :outputs nil
    :fields {
        :filename "test/molder/test/data/csv/smallset2-out.csv"
        :header true
        :separator ";"
        }}}