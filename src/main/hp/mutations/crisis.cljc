(ns hp.mutations.crisis
  (:require #?(:clj [com.wsscode.pathom.connect :as pc :refer [defmutation]]
               :cljs [com.fulcrologic.fulcro.mutations :as pc :refer
                      [defmutation]])
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [taoensso.timbre :as log]
            #?(:clj [hp.db])))

#?(:cljs (defmutation edit-crisis
                      [{:crisis/keys [id] :as crisis}]
                      (action [{:keys [state]}]
                              (swap! state
                                (fn [db]
                                  (update-in db [:crisis/id id] merge crisis))))
                      (remote [env] true))
   :clj (defmutation edit-crisis
                     [env args]
                     {::pc/sym `edit-crisis}
                     (log/debug "Editing crisis:" args)
                     (hp.db/add-docs [args])
                     args))

#?(:cljs (defmutation
           add-crisis
           [_]
           (action
             [{:keys [state]}]
             (swap! state
               (fn [db]
                 (let [uuid (str (random-uuid))]
                   (-> db
                       (assoc-in [:crisis/id uuid]
                                 {:crisis/id uuid
                                  :crisis/text ""
                                  :crisis/description ""})
                       (update-in [:crisis/list] conj [:crisis/id uuid])))))))
   :clj (defmutation add-crisis
                     [env crisis]
                     {::pc/sym `add-crisis}
                     (log/debug "add-crisis:" crisis)
                     (hp.db/add-docs [crisis])
                     crisis))

#?(:cljs (defmutation remove-crisis
                      [{:crisis/keys [id]}]
                      (action [{:keys [state]}]
                              (swap! state
                                (fn [db]
                                  (-> db
                                      (update :crisis/id dissoc uuid)
                                      ;; Gotta make sure it's a vector or edges don't work good
                                      (update-in [:crisis/list]
                                                 #(vec (remove (fn [[_ edge-id]]
                                                                 (= id edge-id))
                                                         %)))))))
                      (remote [env] true))
   :clj (pc/defmutation remove-crisis
                        [env {id :crisis/id :as crisis}]
                        {::pc/sym `remove-crisis}
                        (log/debug "Removing crisis:" crisis)
                        (hp.db/trans {:tx-data [[:db/retractEntity
                                                 [:crisis/id id]]]})
                        [[:crisis/list] [:crisis/id id]]))