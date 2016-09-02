(ns molder.core
  (:require [molder.processing :as processing])
  (:use [molder.node-defs])
  (:gen-class)
  (:use compojure.core)
  (:use ring.util.response)
  (:import org.eclipse.jetty.server.ssl.SslSocketConnector)
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [ring.adapter.jetty :as jetty]
            (ring.middleware [multipart-params :as mp])
            (ring.middleware [params :as rmp])
            (ring.middleware [keyword-params :as krmp])
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.json :as middleware]))


(defn run-mold [ mold ]
    (println "Running mold " mold)
    (let [result (processing/process-mold mold)]
      (if
        (= 0 (count (:errors result))) ; if no errors
        (response result) ; return result
        { :status 500 :body result } ; else return error
        )))

; TODO Error handling: filename is invalid (or non-existing).
; TODO Error handling: if cannot save file
(defn save-mold [ filename mold ]
  ; (println "filename: " filename)
  ; (println "MOLD: " mold)
  (spit filename (with-out-str (pr mold)))
  { :status 201 })

; TODO Error handling: if filename is non-existing or invalid
; TODO Error handling: if file not found
; TODO Error handling: if file is invalid clojure
(defn load-mold [ filename ]
  (response (read-string (slurp filename))))

(defroutes app-routes
  (POST "/run" {body :body} (run-mold body))
  (POST "/save-mold" {body :body params :params} (save-mold (:filename params) body))
  (GET "/load-mold" { params :params } (load-mold (:filename params)))
  (GET "/node-metadata" [] (response all-node-metadata))
  (route/not-found "Not Found"))

(def handler
(wrap-cors app-routes :access-control-allow-origin #"(.)*"
                      :access-control-allow-methods [:get :put :post :delete] ;TODO
                      :access-control-allow-headers ["Content-Type"]))

(defn start-server [& port]
(let [port 8109]
  (jetty/run-jetty (->
                    (handler/api handler)
                    (krmp/wrap-keyword-params)
                    (mp/wrap-multipart-params)
                    (middleware/wrap-json-body {:keywords? true})
                    (middleware/wrap-json-response))
    {:port port })))

; Required for tests to be able to reference 'app'
(def app (rmp/wrap-params (krmp/wrap-keyword-params (middleware/wrap-json-body app-routes {:keywords? true}))))

(defn -main [& [port]]
    (start-server))