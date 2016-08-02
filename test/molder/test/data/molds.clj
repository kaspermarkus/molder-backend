(ns molder.test.data.molds)

; Mold consisting of a single pipe from csv-input via identity to csv-output node
(def test-nodes1
 {:csv-input5293 {
    :type "csv-input",
    :id "csv-input5293"
    :name "Adresses CSV"
    :inputs nil
    :outputs [ :identity9101 ]
    :fields
      { :filename "test/molder/test/data/smallset.csv"
        :header true
        :separator \;
    }}

  :identity9101 {
    :type "identity"
    :id "identity9101"
    :name "No mods"
    :inputs [ :csv-input5293 ]
    :outputs [ :csv-output1892 ]
    :fields {}}

  :csv-output1892 {
    :type "csv-output"
    :id "csv-output1892"
    :name "Modified addresses"
    :inputs [ :identity9101 ]
    :outputs nil
    :fields {
        :filename "test/molder/test/data/smallset-out.csv"
        :header true
        :separator \;
    }}})

; Mold consisting of a two pipes.
; 1) csv-input via identity to identity to csv-output node
; 2) csv-input to csv-output node
(def test-nodes2
 {:csv-input5293 {
    :type "csv-input",
    :id "csv-input5293"
    :name "Adresses CSV"
    :inputs nil
    :outputs [ :identity9101 ]
    :fields
      { :filename "test/molder/test/data/smallset.csv"
        :header true
        :separator \;
    }}
  :identity9101 {
    :type "identity"
    :id "identity9101"
    :name "No mods"
    :inputs [ :csv-input5293 ]
    :outputs [ :identity222 ]
    :fields {}}
  :identity222 {
    :type "identity"
    :id "identity222"
    :name "No mods"
    :inputs [ :identity9101 ]
    :outputs [ :csv-output1892 ]
    :fields {}}
  :csv-output1892 {
    :type "csv-output"
    :id "csv-output1892"
    :name "Modified addresses"
    :inputs [ :identity222 ]
    :outputs nil
    :fields {
        :filename "test/molder/test/data/smallset-out.csv"
        :header true
        :separator \;
    }}


  :csv-input1 {
    :type "csv-input",
    :id "csv-input1"
    :name "my CSV"
    :inputs nil
    :outputs [ :csv-output1 ]
    :fields
      { :filename "test/molder/test/data/smallset2.csv"
        :header true
        :separator \;
      }}
  :csv-output1 {
    :type "csv-output"
    :id "csv-output1"
    :name "Modified addresses"
    :inputs [ :csv-input1 ]
    :outputs nil
    :fields {
        :filename "test/molder/test/data/smallset2-out.csv"
        :header true
        :separator \;
        }}})
