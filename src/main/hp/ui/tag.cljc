(ns hp.ui.tag
  (:require [hp.mutations.tag :as muts]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.mutations :as m]
            #?(:clj [com.fulcrologic.fulcro.dom-server :as dom]
               :cljs [com.fulcrologic.fulcro.dom :as dom])
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defsc Tag
       [this {:tag/keys [id name] :as props} {ident :ident}]
       {:query [:tag/id :tag/name] :ident :tag/id}
       (if ident
         (dom/button :.ui.icon.button
                     {:onClick #(comp/transact! this
                                                [(muts/remove-tag
                                                   {:ident ident :tag/id id})])}
                     name
                     " "
                     (dom/create-element "i" #js {"className" "fas fa-times "}))
         (dom/button :.ui.icon.button name)))
(def ui-tag (comp/factory Tag))

(defsc Tags
       [this tags {ident :ident}]
       {:query [{:tag/id (comp/get-query Tag)}]}
       (dom/div (dom/pre "Tags:" (pr-str tags)) (map ui-tag tags)))
(def ui-tags (comp/factory Tags))

(defsc TagsWrapper
       "Like tags, but for an object that has tags"
       [this {tags :tag/tags list :tag/list :as props} {ident :ident}]
       {:query [{:tag/tags (comp/get-query Tags)}
                {[:tag/list '_] (comp/get-query Tags)}]}
       (dom/div (when-not (keyword? tags)
                  (map #(ui-tag (comp/computed % {:ident ident})) tags))
                (when ident
                  (dom/select
                    :.ui.select
                    (dom/option {:selected true} "Add Tag")
                    (->> list
                         (remove (set tags))
                         (map (fn [{:tag/keys [id name]}]
                                (dom/option
                                  {:onClick (fn [_]
                                              (comp/transact!
                                                this
                                                [(muts/add-tag {:ident ident
                                                                :tag/id id})]))}
                                  name))))))))
(def ui-tagswrapper (comp/factory TagsWrapper))