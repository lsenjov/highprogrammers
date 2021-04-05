(ns hp.ui.tag
  (:require [hp.mutations]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.mutations :as m]
            #?(:clj [com.fulcrologic.fulcro.dom-server :as dom]
               :cljs [com.fulcrologic.fulcro.dom :as dom])
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defsc Tag
  [this {:tag/keys [id name] :as props}]
  {:query [:tag/id :tag/name]
   :ident (fn [] [:tag/id id])}
  (dom/div
   (dom/div "Tag:" (pr-str props))
   (dom/div "id:" id)
   (dom/div "name:" name)))
(def ui-tag (comp/factory Tag))

(defsc Tags
  [this tags]
  {:query [{:tag/id [:tag/id :tag/name]}]}
  (dom/div
   (dom/div "Tags:" (pr-str tags))
   (map ui-tag tags)))
(def ui-tags (comp/factory Tags))

(defsc TagsWrapper
  "Like tags, but for an object that has tags"
  [this {tags :tag/tags :as props}]
  {:query [{:tag/tags (comp/get-query Tags)}]}
  (dom/div
   (dom/div "TagsWrapper " (pr-str tags))
   (ui-tags (->> tags (map (fn [{id :tag/id}] [:tag/id id]))))
   (dom/button :.ui.button {} "Add tags")))
(def ui-tagswrapper (comp/factory TagsWrapper))