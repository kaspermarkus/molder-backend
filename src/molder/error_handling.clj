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
