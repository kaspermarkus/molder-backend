(ns molder.test.data.simpletables)

(def tinytable1
  { :columns
     { :Name { :index 0 }
       :Age { :index 1 }
       :Country { :index 2 }}
    :data
      '( [ "Kasper" "31" "Switzerland" ]
         [ "Kevin" "31" "Denmark" ]
         [ "Santa Clause" "800" "North Pole"]) })

(def tinytable2
  { :columns
     { :Region { :index 0 }
       :UnitCost { :index 1 }}
    :data
      '( [ "East" "1.99" ]
         [ "North-Central" "19.99" ]
         [ "Mid-Central" "4.99" ]
         [ "Central", "19.99" ]
         [ "West" "2.99" ])})