(ns hp.ui.crisis
  (:require [hp.mutations]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.mutations :as m]
            #?(:clj [com.fulcrologic.fulcro.dom-server :as dom]
               :cljs [com.fulcrologic.fulcro.dom :as dom])
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defsc Crisis
       [this {:crisis/keys [id text description] :as props}]
       {:query [:crisis/id :crisis/text :crisis/description]
        :ident (fn [] [:crisis/id id])}
       (println "this:" this)
       (println "stuff:" id text description)
       (dom/div (dom/div "Crisis id: " id)
                (dom/div (or text "No text"))
                (dom/div (or description "No description"))))
(def ui-crisis (comp/factory Crisis))

(defsc CrisisForm
       [this {:crisis/keys [id text description] :as props}]
       {:query [:crisis/id :crisis/text :crisis/description]
        :ident (fn [] [:crisis/id id])
        :form-fields #{:crisis/text :crisis/description}}
       (println "CrisisForm:" props)
       (dom/div (dom/div "Crisis id:" id) (dom/input {:value text})))

(defn field
  [{:keys [label valid? error-message] :as props}]
  (let [input-props (-> props
                        (assoc :name label)
                        (dissoc :label :valid? :error-message))]
    (dom/div :.ui.field
             (dom/label {:htmlFor label} label)
             (dom/input input-props)
             (dom/div :.ui.error.message
                      {:classes [(when valid? "hidden")]}
                      error-message))))
(defsc
  CrisisForm2
  [this {:crisis/keys [id text description] :as props}]
  {:query [:crisis/id :crisis/text :crisis/description fs/form-config-join]
   :ident [:crisis/id :crisis/id]
   :initial-state (fn [_]
                    (fs/add-form-config CrisisForm2
                                        {:crisis/id (int (rand 1000))
                                         :crisis/text ""
                                         :crisis/description ""}))
   :form-fields #{:crisis/id :crisis/text :crisis/description}}
  (let [apply-fn! (fn [evt]
                    (println "lololol")
                    (comp/transact! this
                                    [(hp.mutations/edit-crisis
                                       {:crisis/id id
                                        :crisis/text text
                                        :crisis/description description})]))]
    (dom/div
      (dom/h3 "Crisis form")
      (dom/div
        :.ui.form
        (field {:label "Text"
                :value (or text "")
                :valid? identity
                :error-message "Please enter a text"
                :onChange #(m/set-string! this :crisis/text :event %)})
        (field {:label "Description"
                :value (or description "")
                :valid? identity
                :error-message "Please enter a description"
                :onChange #(m/set-string! this :crisis/description :event %)})
        (dom/button :.ui.primary.button
                    {:onClick #(apply-fn! true)}
                    "Apply")))))
(def ui-crisis-form (comp/factory CrisisForm2))


(defsc CrisisList
       [this crisises]
       {:query [:crisis/list {:crisis/id (comp/get-query Crisis)}]}
       (println "CrisisList:" crisises)
       (map ui-crisis-form crisises))
(def ui-crisis-list (comp/factory CrisisList))


