(ns hp.ui.crisis
  (:require [hp.mutations]
            [hp.ui.tag :as tag]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.mutations :as m]
            #?(:clj [com.fulcrologic.fulcro.dom-server :as dom]
               :cljs [com.fulcrologic.fulcro.dom :as dom])
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defsc Crisis
       [this {:crisis/keys [id text description] :as props}]
       {:query [:crisis/id :crisis/text :crisis/description
                {:tag/tags (comp/get-query tag/Tag)} :tag/id->e]
        :ident :crisis/id}
       (dom/div (dom/div (or text "No text"))
                (dom/div (or description "No description"))
                (dom/h4 "tagswrapper")
                (tag/ui-tagswrapper props)))
(def ui-crisis (comp/factory Crisis))

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
  CrisisForm
  [this {:crisis/keys [id text description] :as props}]
  {:query [:crisis/id :crisis/text :crisis/description fs/form-config-join]
   :ident [:crisis/id :crisis/id]
   :initial-state (fn [_]
                    (fs/add-form-config
                      CrisisForm
                      {:crisis/id "" :crisis/text "" :crisis/description ""}))
   :form-fields #{:crisis/text :crisis/description}}
  (let [apply-fn! (fn [evt]
                    (println "lololol")
                    (comp/transact! this
                                    [(hp.mutations/edit-crisis
                                       {:crisis/id id
                                        :crisis/text text
                                        :crisis/description description})]))]
    (dom/div
      (dom/h3 "Crisis form")
      (dom/pre "debug:" (pr-str props))
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
                :onChange #(m/set-string! this :crisis/description :event %)})
        (dom/button :.ui.primary.button {:onClick #(apply-fn! true)} "Apply")
        (dom/button
          :.ui.warning.button
          {:onClick #(comp/transact! this [(hp.mutations/remove-crisis props)])}
          "Delete"))
      (tag/ui-tagswrapper props))))
(def ui-crisis-form (comp/factory CrisisForm))


(defsc
  CrisisList
  [this crisises]
  {:query [:crisis/id {:crisis/id (comp/get-query Crisis)}]}
  (println "CrisisList:" crisises)
  (dom/div (map ui-crisis-form crisises)
           (map ui-crisis crisises)
           (dom/div
             (dom/button
               :.ui.button
               {:onClick #(comp/transact! this [(hp.mutations/add-crisis nil)])}
               "Add empty crisis"))))
(def ui-crisis-list (comp/factory CrisisList))
