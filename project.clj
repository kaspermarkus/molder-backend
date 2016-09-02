(defproject molder "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  ; TODO check imports
  :dependencies [[compojure "1.5.1"] ;check
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/data.csv "0.1.2"]
                 [org.clojure/tools.trace "0.7.9"] ;check
                 [slingshot "0.12.2"] ;exception handling
                 [environ "1.0.0"] ;check
                 [com.spoon16/clj-gson "0.0.1"] ;check
                 [org.clojure/data.json "0.2.5"] ;check
                 [cheshire "5.1.1"] ;check - perhaps only necessary for tests
                 [ring-cors "0.1.4"] ;check
                 [ring/ring-core "1.2.1"] ;check
                 [ring/ring-json "0.3.1"] ;check
                 [ring/ring-jetty-adapter "1.2.1"] ;check
                 [org.clojure/data.generators "0.1.2"] ;check
                 [clj-http "2.0.0"] ;check
                 [clj-http-fake "1.0.1"]] ;check
                 ;;; From my project.clj
  :profiles {
    :dev {
      :dependencies [[ring-mock "0.1.5"]]}}
  :main molder.core) ; exception handling

; TODO:
; Add license info, github references, etc
