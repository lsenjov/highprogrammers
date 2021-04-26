(ns hp.ui.crisis
  (:require [hp.mutations.crisis :as muts]
            [hp.ui.tag :as tag]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.mutations :as m]
            #?(:clj [com.fulcrologic.fulcro.dom-server :as dom]
               :cljs [com.fulcrologic.fulcro.dom :as dom])
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]
            [taoensso.timbre :as log]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]))

(defsc
  Crisis
  [this {:crisis/keys [id text description] :as props} {editable? :editable?}]
  {:query [:crisis/id :crisis/text :crisis/description
           {:tag/tags (comp/get-query tag/Tag)}
           {[:tag/list '_] (comp/get-query tag/Tag)}]
   :ident :crisis/id
   :route-segment ["crisis" :crisis/id]
   :route-cancelled (fn [{:crisis/keys [id]}]
                      (log/info "Routing cancelled to user " id))
   :will-enter (fn [app {:crisis/keys [id] :as route-params}]
                 (log/info "Will enter user with route params " route-params)
                 (dr/route-deferred [:crisis/id id]
                                    #(df/load app
                                              [:crisis/id id]
                                              Crisis
                                              {:post-mutation `dr/target-ready
                                               :post-mutation-params
                                                 {:target [:crisis/id id]}})))}
  (dom/div (dom/div (or text "No text"))
           (dom/div (or description "No description"))
           (tag/ui-tagswrapper props)
           (when editable?
             (dom/button {:onClick #(dr/change-route! this ["crisis" id])}
                         "Edit"))))
(def ui-crisis (comp/factory Crisis))

(defn field
  [{:keys [label valid? error-message type] :as props}]
  (let [input-props (-> props
                        (assoc :name label)
                        (dissoc :label :valid? :error-message))]
    (dom/div :.ui.field
             (dom/label {:htmlFor label} label)
             ((case type
                :area dom/textarea
                ;; Else
                dom/input)
               input-props)
             (dom/div :.ui.error.message
                      {:classes [(when valid? "hidden")]}
                      error-message))))
(defsc
  CrisisForm
  [this {:crisis/keys [id text description] :as props}]
  {:query [:crisis/id :crisis/text :crisis/description fs/form-config-join]
   :ident :crisis/id
   :initial-state (fn [_]
                    (fs/add-form-config
                      CrisisForm
                      {:crisis/id "" :crisis/text "" :crisis/description ""}))
   :form-fields #{:crisis/text :crisis/description}
   :route-segment ["crisis" :crisis/id]
   :route-cancelled (fn [{:crisis/keys [id]}]
                      (log/info "Routing cancelled to user " id))
   :will-enter (fn [app {:crisis/keys [id] :as route-params}]
                 (log/info "Will enter user with route params " route-params)
                 (dr/route-deferred [:crisis/id id]
                                    #(df/load app
                                              [:crisis/id id]
                                              Crisis
                                              {:post-mutation `dr/target-ready
                                               :post-mutation-params
                                                 {:target [:crisis/id id]}})))}
  (let [apply-fn! (fn [evt]
                    (println "lololol")
                    (comp/transact! this
                                    [(muts/edit-crisis
                                       {:crisis/id id
                                        :crisis/text text
                                        :crisis/description description})]))]
    (dom/div
      (dom/h3 "Crisis form")
      (dom/div (pr-str props))
      (dom/div
        :.ui.form
        (field {:label "Text"
                :value (or text "")
                :valid? (boolean (not-empty text))
                :error-message "Please enter a text"
                :onChange #(m/set-string! this :crisis/text :event %)})
        (field {:label "Description"
                :value (or description "")
                :valid? (boolean (not-empty description))
                :error-message "Please enter a description"
                :type :area
                :onChange #(m/set-string! this :crisis/description :event %)})
        (dom/button :.ui.primary.button {:onClick #(apply-fn! true)} "Apply")
        (dom/button :.ui.warning.button
                    {:onClick #(comp/transact! this
                                               [(muts/remove-crisis props)])}
                    "Delete"))
      (tag/ui-tagswrapper (comp/computed props
                                         ;; Add ident so it knows its editable
                                         {:ident (comp/get-ident this)})))))
(def ui-crisis-form (comp/factory CrisisForm))


(defsc CrisisList
       [this {list :crisis/list crisises :crisis/ids :as props}]
       {:query [:crisis/list {:crisis/ids (comp/get-query Crisis)}]
        :route-segment ["crisislist" :crisis/list]
        :ident :crisis/list
        :will-enter
          (fn [app {:crisis/keys [list] :as route-params}]
            (log/info "Will enter user with route params " route-params)
            (dr/route-deferred [:crisis/list list]
                               #(df/load! app
                                          :crisis/list
                                          CrisisList
                                          {:post-mutation `dr/target-ready
                                           :post-mutation-params
                                             {:target [:crisis/list list]}})))}
       (dom/div
         (dom/h3 "Crisises: " list)
         (dom/pre "CrisisList:" (pr-str props))
         (dom/div
           (map #(ui-crisis (comp/computed % {:editable? true})) crisises)
           (dom/div (dom/button
                      :.ui.button
                      {:onClick #(comp/transact! this [(muts/add-crisis nil)])}
                      "Add empty crisis")))))
(def ui-crisis-list (comp/factory CrisisList))
(comment (df/load! hp.application/app :crisis/list CrisisList))
