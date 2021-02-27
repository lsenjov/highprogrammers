(ns hp.db
  "Initial DB data"
  (:require
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [datomic.client.api :as dato]))

(def initial-db
  {:crisis/list
   {"first" {:crisis/id "first"
             :crisis/text "A test crisis"
             :crisis/description "A much longer description here"}
    "second" {:crisis/id "second"
              :crisis/text "A second test crisis"
              :crisis/description "A much longer description here"}}})

(def *db (atom initial-db))

(def client
  (dato/client
    {:server-type :dev-local
     :system "dev"
     :storage-dir :mem
     }))
(def db-name "devdb")
(dato/delete-database
  client
  {:db-name db-name})
(dato/create-database
  client
  {:db-name db-name})
(def conn
  (dato/connect
    client
    {:db-name db-name}))

(def crisis-schema
  [{:db/ident :crisis/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "A crisis id"}
   {:db/ident :crisis/text
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "A crisis text, to be read to players"}
   {:db/ident :crisis/description
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "A crisis description"}])
(dato/transact conn {:tx-data crisis-schema})
(dato/transact
  conn
  {:tx-data [{:crisis/id "first"
              :crisis/text "A test crisis"
              :crisis/description "A much longer description here"}
             {:crisis/id "second"
              :crisis/text "A second test crisis"
              :crisis/description "A much longer description here"}]})
(defn q
  ([query args]
   (dato/q
     {:query query
      :args  (concat [(dato/db conn)] args)}))
  ([query]
   (q query [])))