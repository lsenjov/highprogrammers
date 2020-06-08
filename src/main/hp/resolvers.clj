(ns hp.resolvers
  (:require
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc]
    [hp.db :refer [*db]]))

(def db
  (atom
    {:people
     {1 {:person/id 1 :person/name "Sally" :person/age 32}
      2 {:person/id 2 :person/name "Joe" :person/age 22}
      3 {:person/id 3 :person/name "Fred" :person/age 11}
      4 {:person/id 4 :person/name "Bobby" :person/age 55}}
     :relationships
     {:friends {:list/id     :friends
                :list/label  "Friends"
                :list/people [1 2]}
      :enemies {:list/id     :enemies
                :list/label  "Enemies"
                :list/people [4 3]}}}))

;; Given :person/id, this can generate the details of a person
(pc/defresolver person-resolver [env {:person/keys [id]}]
  {::pc/input  #{:person/id}
   ::pc/output [:person/name :person/age]}
  (get-in @db [:people id]))

;; Given a :list/id, this can generate a list label and the people
;; in that list (but just with their IDs)
(pc/defresolver list-resolver [env {:list/keys [id]}]
  {::pc/input  #{:list/id}
   ::pc/output [:list/label {:list/people [:person/id]}]}
  (when-let [list (get-in @db [:relationships id])]
    (assoc list
      :list/people (mapv (fn [id] {:person/id id}) (:list/people list)))))

(pc/defresolver friends-resolver [env input]
  {::pc/output [{:friends [:list/id]}]}
  {:friends {:list/id :friends}})

(pc/defresolver enemies-resolver [env input]
  {::pc/output [{:enemies [:list/id]}]}
  {:enemies {:list/id :enemies}})

(pc/defresolver crisis-resolver [env {:crisis/keys [id] :as input}]
  {::pc/input #{:crisis/id}
   ::pc/output [:crisis/id :crisis/text :crisis/description]}
  (get-in @*db [:crisis/list id]))

(pc/defresolver crisis-all-resolver [env input]
  {::pc/output [{:crisis/list [:crisis/id :crisis/text :crisis/description]}]}
  {:crisis/list
   (->> @*db
        :crisis/list
        keys
        (mapv (fn [id] {:crisis/id id})))})

(def resolvers [person-resolver
                list-resolver
                friends-resolver
                enemies-resolver
                crisis-resolver
                crisis-all-resolver
                ])
