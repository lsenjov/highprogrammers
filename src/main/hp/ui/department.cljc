(ns hp.ui.department
  (:require [hp.mutations.crisis :as muts]
            [hp.ui.tag :as tag]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.mutations :as m]
            #?(:clj [com.fulcrologic.fulcro.dom-server :as dom]
               :cljs [com.fulcrologic.fulcro.dom :as dom])
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defsc Department
       [this {:department/keys [id full-name acronym]}]
       {:query [:department/id :department/full-name :department/acronym
                {:tag/tags (comp/get-query tag/Tag)}
                {[:tag/list '_] (comp/get-query tag/Tag)}]
        :ident :department/id}
       (dom/div (dom/div (or full-name "No name"))
                (dom/div (or acronym "No acronym"))))