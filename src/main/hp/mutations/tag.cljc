(ns hp.mutations.tag
  (:require #?(:clj [com.wsscode.pathom.connect :as pc :refer [defmutation]]
               :cljs [com.fulcrologic.fulcro.mutations :as pc :refer
                      [defmutation]])
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [taoensso.timbre :as log]
            #?(:clj [hp.db])))

#?(:cljs (defmutation add-tag
                      [{ident :ident id :tag/id :as tag}]
                      (action [{:keys [state]}]
                              (swap! state (fn [db]
                                             (update-in
                                               db
                                               (concat ident [:tag/tags])
                                               (fn [tags]
                                                 (-> tags
                                                     ;; Remove the edge if it exists
                                                     (conj [:tag/id id])
                                                     ;; Ensure no duplicates
                                                     distinct
                                                     ;; Make sure it stays a vector
                                                     vec))))))
                      (remote [env] true))
   :clj (defmutation add-tag
                     [env {ident :ident id :tag/id}]
                     {::pc/sym `add-tag}
                     (log/debug "Add-tag:" ident id)
                     (hp.db/trans {:tx-data [[:db/add ident :tag/tags
                                              [:tag/id id]]]})
                     {}))
#?(:cljs (defmutation remove-tag
                      ;; Ident is the ident of the parent we're performing this on
                      ;; Id is the id of the tag
                      [{ident :ident id :tag/id}]
                      (action [{:keys [state]}]
                              (println "remove-tag" ident id)
                              (swap! state merge/remove-ident*
                                [:tag/id id]
                                (vec (concat ident [:tag/tags]))))
                      (remote [env] true))
   :clj (defmutation remove-tag
                     [env {ident :ident id :tag/id}]
                     {::pc/sym `remove-tag}
                     (log/debug "Remove-tag:" ident id)
                     (hp.db/trans {:tx-data [[:db/retract ident :tag/tags
                                              [:tag/id id]]]})
                     {}))