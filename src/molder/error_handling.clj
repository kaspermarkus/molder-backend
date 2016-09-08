(ns molder.error-handling
  (:require
    [slingshot.slingshot :as slingshot]
    [molder.utils :as utils]))

(defn throw-warning [ info ]
  (slingshot/throw+ (assoc-in info [ :severity ] :warning)))

(defn throw-error [ info ]
  (slingshot/throw+ (assoc-in info [ :severity ] :error)))

(defn add-error [state info]
    (utils/add-error-to-state info state))

(defn add-warning [state info]
    (utils/add-warning-to-state info state))

(defn add-parameter-error [state node field & desc]
    (println (apply str desc))
    (add-error state {:type :parameter-error
                      :field field
                      :description (apply str desc)
                      :node (:id node) }))