(ns hp.mutations
  (:require
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]))

(defmutation delete-person
  "Mutation: Delete the person with `:person/id` from the list with `:list/id`"
  [{list-id   :list/id
    person-id :person/id}]
  (action [{:keys [state]}]
          (println "state:" @state)
          (println list-id person-id)
          (swap! state
                 merge/remove-ident*
                 [:person/id person-id]
                 [:list/id list-id :list/people]))
  (remote [env] true))
(defmutation delete-person-wssync
  [{list-id   :list/id
    person-id :person/id}]
  (action [{:keys [state]}]
          (println "state:" @state)
          (println list-id person-id)
          (swap! state
                 merge/remove-ident*
                 [:person/id person-id]
                 [:list/id list-id :list/people])))

(defmutation add-person
  [{list-id :list/id
    {person-id :person/id :as person} :person}]
  (action [{:keys [state]}]
          (println "state:" @state)
          (println "person-id:" person-id)
          (when person-id
            (swap! state
                   (fn [db]
                     (-> db
                         (assoc-in
                           [:person/id person-id]
                           person)
                         (update-in
                           [:list/id list-id :list/people]
                           conj
                           [:person/id person-id]))))))
  (remote [env] true))
(defmutation add-person-wssync
  [{list-id :list/id
    {person-id :person/id :as person} :person}]
  (action [{:keys [state]}]
          (println "state:" @state)
          (println "person-id:" person-id)
          (when person-id
            (swap! state
                   (fn [db]
                     (-> db
                         (assoc-in
                           [:person/id person-id]
                           person)
                         (update-in
                           [:list/id list-id :list/people]
                           conj
                           [:person/id person-id])))))))
