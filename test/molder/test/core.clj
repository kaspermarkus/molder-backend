(ns molder.test.core
  (:use [clojure.test]
        [molder.node-defs]
        [slingshot.slingshot :only [throw+ try+]])
  (:require
        [ring.mock.request :as mock]
        [clojure.data.json :as json]
        [molder.core :refer :all]
        [molder.test-utils :as test-utils]))


(defn json-body
  "Adds a body to the request as well as the 'application/json' content type"
  [request new-body]
  (mock/content-type (mock/body request new-body) "application/json")) ;TODO - use fancy -> here

; GET, POST, PUT and DELETE mock call functionality:
(defn do-get-request
  "Does a mock GET request to the given url and returns response"
  ([url] (app (mock/request :get url)))
  ([url params] (app (mock/request :get url params))))

(defn do-post-request
  "Does a mock POST request to the given url with the content of the given file as content and
   and returns response. The file location should be relative to one of the resource folders"
   [url payload]
   (app (json-body (mock/request :post url) (json/write-str payload))))

(deftest load-mold-test
  (let [get-resp (do-get-request "/load-mold" { :filename "test/molder/test/data/mold2.clj"})
        expect-body (read-string (slurp "test/molder/test/data/mold2.clj"))]
    (test-utils/assert-status get-resp 200)
    (test-utils/assert-body get-resp expect-body)))

(deftest save-mold-test
  (let [body (read-string (slurp "test/molder/test/data/mold2.clj"))
        resp (do-post-request "/save-mold?filename=test/molder/test/data/mold2-posted.clj" body)]
    (test-utils/assert-status resp 201)
    (is (= body (read-string (slurp "test/molder/test/data/mold2-posted.clj"))))))

(deftest node-metadata-test
  (let [get-resp (do-get-request "/node-metadata")]
    (test-utils/assert-body get-resp all-node-metadata)
    (test-utils/assert-status get-resp 200)))
    ; (test-utils/diff-out all-node-metadata get-resp)))