(ns hp.ui.tag
  (:require [hp.mutations]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.mutations :as m]
            #?(:clj [com.fulcrologic.fulcro.dom-server :as dom]
               :cljs [com.fulcrologic.fulcro.dom :as dom])
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defsc Tag
       [this {:tag/keys [id name] :as props}]
       {:query [:tag/id :tag/name] :ident :tag/id}
       (dom/div (dom/pre "Tag:" (pr-str props))
                (dom/div "id:" id)
                (dom/div "name:" name)))
(def ui-tag (comp/factory Tag))

(defsc Tags
       [this tags]
       {:query [{:tag/id (comp/get-query Tag)}]}
       (dom/div (dom/pre "Tags:" (pr-str tags)) (map ui-tag tags)))
(def ui-tags (comp/factory Tags))

(defsc TagsWrapper
       "Like tags, but for an object that has tags"
       [this {tags :tag/id->e :as props}]
       {:query [{:tag/id->e (comp/get-query Tags)}]}
       (dom/div (dom/pre "TagsWrapper " (pr-str tags))
                (when-not (keyword? tags) (ui-tags tags))
                (dom/button :.ui.button {} "Add tags")))
(def ui-tagswrapper (comp/factory TagsWrapper))