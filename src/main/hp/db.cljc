(ns hp.db
  "Initial DB data"
  (:require
    [com.fulcrologic.fulcro.algorithms.merge :as merge]))

(def initial-db
  {:crisis/id
   {"first" {:crisis/id "first"
             :crisis/text "A test crisis"
             :crisis/description "A much longer description here"}
    "second" {:crisis/id "second"
              :crisis/text "A second test crisis"
              :crisis/description "A much longer description here"}}})

(def *db (atom initial-db))
