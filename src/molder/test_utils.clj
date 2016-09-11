(ns molder.test-utils
  (:use [clojure.test])
  (:require
        [ring.mock.request :as mock]
        [clojure.data.json :as json]
        [clojure.data :as data]
        [molder.core :refer :all]))

  (defn assert-body
  "compares the :body value of the 'response' parameter to the 'exp-body' parameter"
  [response exp-body]
  (is (= exp-body (:body response))))

(defn assert-status
  "Asserts the status of the response. Returns the unmodified response for chaining"
  [response exp-status]
  (is (= exp-status (:status response)))
  response)


(defn do-get-request-and-check-response [assert-desc url expected-body expected-status]
  (let [response (app (mock/request :get url))]
    (is (= expected-body (:body response)) (str assert-desc " - checking body"))
    (is (= expected-status (:status response)) (str assert-desc " - checking status"))
    response))

(defn diff-out [ a b ]
  (let [diff (data/diff a b)]
    (println "DIFF - first only: " (first diff))
    (println "DIFF - second only: " (second diff))))
    ; (println "DIFF:\nFirst: " (first diff) "\nSecond: " (second diff) "\nLast " (nth diff 3))))

(def state-template { :data {} :errors [] :warnings [] })

(defn clear-state [state] (swap! state (fn [_] state-template)))