(ns hp.ui
  (:require [hp.mutations :as api]
            [hp.ui.crisis :as crisis]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            #?(:clj [com.fulcrologic.fulcro.dom-server :as dom]
               :cljs [com.fulcrologic.fulcro.dom :as dom])))

(defsc Person
       [this {:person/keys [id name age] :as props} {:keys [onDelete]}]
       {:query [:person/id :person/name :person/age]
        :ident (fn [] [:person/id (:person/id props)])}
       (dom/li (dom/h5 (str name " (age: " age ")")
                       (dom/button {:className "btn btn-primary btn-sm"
                                    :onClick #(onDelete id)}
                                   "X"))))

(def ui-person (comp/factory Person {:keyfn :person/id}))

(defsc
  PersonList
  [this {:list/keys [id label people] :as props}]
  {:query [:list/id :list/label {:list/people (comp/get-query Person)}]
   :ident (fn [] [:list/id (:list/id props)])}
  (let [delete-person (fn [person-id]
                        (comp/transact! this
                                        [(api/delete-person {:list/id id
                                                             :person/id
                                                               person-id})]))]
    (dom/div
      (dom/h4 label)
      (dom/button {:onClick #(comp/transact!
                               this
                               [(api/add-person
                                  {:list/id id
                                   :person {:person/id (rand-nth (range 200))
                                            :person/age (rand-nth (range 200))
                                            :person/name
                                              (str "Something "
                                                   (rand-nth (range 200)))}})])}
                  "Add random person")
      (dom/ul (map #(ui-person (comp/computed % {:onDelete delete-person}))
                people)))))

(def ui-person-list (comp/factory PersonList))

(defsc
  PersonAdder
  [this _]
  {}
  (dom/button
    {:onClick #(comp/transact!
                 this
                 [(api/add-person
                    {:person/id (rand-nth (range 200))
                     :person/age (rand-nth (range 200))
                     :person/name (str "Something " (rand-nth (range 200)))})])}
    "Add random person"))
(def ui-person-adder (comp/factory PersonAdder))

(defsc Debug
       [this props]
       {:query []}
       (dom/div #?(:cljs (with-out-str
                           (cljs.pprint/pprint
                             (com.fulcrologic.fulcro.application/current-state
                               hp.application/app))))))
(def ui-debug (comp/factory Debug))

(defsc Root
       [this {:keys [friends enemies] crisises :crisis/list :as props}]
       {:query [{:friends (comp/get-query PersonList)}
                {:enemies (comp/get-query PersonList)}
                {:crisis/list (comp/get-query crisis/Crisis)}]
        :initial-state {}}
       (dom/div
         ;; (dom/h3 "Friends")
         ;; (when friends (ui-person-list friends))
         ;; (dom/h3 "Enemies")
         ;; (when enemies (ui-person-list enemies))
         (dom/h3 "Crisises")
         (when crisises (crisis/ui-crisis-list crisises))
         (dom/pre (ui-debug))))
