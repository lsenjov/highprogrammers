(ns hp.db
  "Initial DB data"
  (:require [datomic.client.api :as dato]))

(def client
  (dato/client {:server-type :dev-local :system "dev" :storage-dir :mem}))
(def db-name "devdb")
(dato/delete-database client {:db-name db-name})
(dato/create-database client {:db-name db-name})
(def conn (dato/connect client {:db-name db-name}))

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
(def tag-schema
  [{:db/ident :tag/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "A tag id"}
   {:db/ident :tag/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/value
    :db/doc "A tag's name"}
   {:db/ident :tag/tags
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc "An object's attached tags"}])
(def all-schema (concat crisis-schema tag-schema))
(dato/transact conn {:tx-data all-schema})
(dato/transact
  conn
  {:tx-data
     [{:db/id "tempclassic" :tag/id "classic" :tag/name "Classic"}
      {:tag/id "straight" :tag/name "Straight"}
      {:crisis/id "first"
       :crisis/text "A test crisis"
       :crisis/description "A much longer description here"
       :tag/tags ["tempclassic"]}
      {:crisis/id "second"
       :crisis/text "A second test crisis"
       :crisis/description
         "A much longer description here\npossibly going over multiple lines"}]})
(defn q
  ([query args] (dato/q {:query query :args (concat [(dato/db conn)] args)}))
  ([query] (q query [])))

(comment
  (q '{:find [(pull ?crisis [* {:tag/tags [*]}])]
       ;:in [$ ?id]
       :where [[?crisis :crisis/id _]]})
  (dato/pull (dato/db conn)
             [:crisis/text {:tag/tags [:tag/id]}]
             [:crisis/id "first"])
  (q '{:find [(pull ?crisis [:crisis/id {:tag/tags [:tag/id]}])]
       ;:in [$ ?id]
       :where [[?crisis :crisis/id _]]})
  (q '{:find [(pull ?tag [:tag/id :tag/name])] :where [[?tag :tag/id _]]}))

(defn add-docs
  "Simple way to add basic documents"
  [data]
  (dato/transact conn {:tx-data data}))

(defn trans [transaction] (dato/transact conn transaction))

(comment (trans {:tx-data [[:db/add [:crisis/id "first"] :tag/tags
                            [:tag/id "straight"]]]}))