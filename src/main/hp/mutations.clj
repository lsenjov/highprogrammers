(ns hp.mutations
  (:require [hp.resolvers :refer [db]]
            [hp.db]
            [com.wsscode.pathom.connect :as pc]
            [taoensso.timbre :as log]))

(pc/defmutation
  delete-person
  [env {list-id :list/id person-id :person/id}]
  ;; Pathom registers these mutations in an index. The key that
  ;; the mutation is
  ;; indexed by can be overridden with the `::pc/sym`
  ;; configuration option below. Note, however, that mutation we
  ;; are sending
  ;; in the `comp/transact!` from the PersonList component above
  ;; is
  ;; `[(api/delete-person ,,,)]` which will expand to the fully
  ;; qualified
  ;; mutation of `[(app.mutations/delete-person ,,,)]`. If you
  ;; encounter unexpected error messages about mutations not being
  ;; found,
  ;; ensure any overridden syms match the expanded namespaces of
  ;; your mutations.
  {::pc/sym `delete-person}
  (log/info "Deleting person" person-id "from list" list-id)
  (swap! db update-in
    [:relationships list-id :list/people]
    (fn [old-list] (filterv #(not= person-id %) old-list))))

(pc/defmutation
  add-person
  [env {list-id :list/id person :person}]
  {::pc/sym `add-person}
  (log/info "Adding person:" person)
  (when-let [person-id (:person/id person)]
    (log/info "Has id:" person-id)
    (swap! db (fn [db]
                (-> db
                    (assoc-in [:people person-id] person)
                    (update-in [:relationships list-id :list/people]
                               conj
                               person-id))))))

(pc/defmutation edit-crisis
                [env args]
                {::pc/sym `edit-crisis}
                (tap> args)
                (log/info "Editing crisis:" args)
                (println "Editing crisis:" args)
                (println (hp.db/add-docs [args]))
                (println "Sent")
                args)

(pc/defmutation remove-crisis
                [env {id :crisis/id :as crisis}]
                {::pc/sym `remove-crisis}
                (tap> crisis)
                (println "Removing crisis:" crisis)
                (hp.db/trans {:tx-data [[:db/retractEntity [:crisis/id id]]]})
                [[:crisis/list] [:crisis/id id]])

(pc/defmutation add-crisis
                [env crisis]
                {::pc/sym `add-crisis}
                (tap> crisis)
                (println (hp.db/add-docs [crisis]))
                crisis)

(pc/defmutation add-tag
                [env {ident :ident id :tag/id}]
                {::pc/sym `add-tag}
                (println "Add-tag:" ident id)
                (hp.db/trans {:tx-data [[:db/add ident :tag/tags
                                         [:tag/id id]]]})
                {})
(pc/defmutation remove-tag
                [env {ident :ident id :tag/id}]
                {::pc/sym `remove-tag}
                (println "Remove-tag:" ident id)
                (hp.db/trans {:tx-data [[:db/retract ident :tag/tags
                                         [:tag/id id]]]}))


(def mutations
  [delete-person add-person edit-crisis remove-crisis add-tag remove-tag])
