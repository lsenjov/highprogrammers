(ns hp.mutations
  (:require [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [com.fulcrologic.fulcro.algorithms.merge :as merge])
  (:require-macros [hp.mutations.macros :refer [def-wssync-mutation]]))

(def-wssync-mutation
  delete-person
  "Mutation: Delete the person with `:person/id` from the list with `:list/id`"
  [{list-id :list/id person-id :person/id}]
  (action [{:keys [state]}]
          (println "state:" @state)
          (println list-id person-id)
          (swap! state merge/remove-ident*
            [:person/id person-id]
            [:list/id list-id :list/people]))
  (remote [env] true))

(def-wssync-mutation
  add-person
  [{list-id :list/id {person-id :person/id :as person} :person}]
  (action [{:keys [state]}]
          (println "state:" @state)
          (println "person-id:" person-id)
          (when person-id
            (swap! state (fn [db]
                           (-> db
                               (assoc-in [:person/id person-id] person)
                               (update-in [:list/id list-id :list/people]
                                          conj
                                          [:person/id person-id]))))))
  (remote [env] true))

(defmutation edit-crisis
             [{:crisis/keys [id] :as crisis}]
             (action [{:keys [state]}]
                     (swap! state
                       (fn [db] (update-in db [:crisis/id id] merge crisis))))
             (remote [env] true))

(defmutation
  add-crisis
  [_]
  (action [{:keys [state]}]
          (swap! state
            (fn [db]
              (let [uuid (str (random-uuid))]
                (-> db
                    (assoc-in
                      [:crisis/id uuid]
                      {:crisis/id uuid :crisis/text "" :crisis/description ""})
                    (update-in [:crisis/list] conj [:crisis/id uuid])))))))

(defmutation
  remove-crisis
  [{:crisis/keys [id]}]
  (action
    [{:keys [state]}]
    (swap! state
      (fn [db]
        (-> db
            (update :crisis/id dissoc uuid)
            ;; Gotta make sure it's a vector or edges don't work good
            (update-in [:crisis/list]
                       #(vec (remove (fn [[_ edge-id]] (= id edge-id)) %)))))))
  (remote [env] true))
