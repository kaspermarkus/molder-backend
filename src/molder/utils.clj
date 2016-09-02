(ns molder.utils)

(defn headers-from-list [first-line] ; TODO test
  (apply hash-map ; create map from list of values: '( header1 val1 header2 val2 ... )
    (flatten ; from '( [header1 val1] [header2 val2] .. ) to flat list
      (map-indexed
        (fn [idx val] [ (keyword val) { :index idx } ])
        first-line))))


(defn headers-to-list [ headers ] ; TODO Tests
  (reduce-kv
    ; Takes vector (retvec) and replaces the (:index v)th element with the value k
    (fn [ retvec k v ] (assoc retvec (:index v) (name k)))
    (vec (range (count headers))) ; create vector of smae length as headers
    headers))


(defn index-from-column-name [ cname table ]
  (get-in table [ :columns (keyword cname) :index ]))


(defn add-warning-to-state [ warn state ]
  (swap! state (fn [st] (assoc-in st [ :warnings ] (conj (:warnings st) warn))))
   (println "Adding warning to state: " warn))

(defn add-error-to-state [ err state ]
  (swap! state (fn [st] (assoc-in st [ :errors ] (conj (:errors st) err))))
   (println "Adding error to state: " err))