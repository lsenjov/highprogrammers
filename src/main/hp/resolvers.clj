(ns hp.resolvers
  (:require [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.connect :as pc]
            [taoensso.timbre :as log]
            [hp.db :as db]))

(def db
  (atom
    {:people {1 {:person/id 1 :person/name "Sally" :person/age 32}
              2 {:person/id 2 :person/name "Joe" :person/age 22}
              3 {:person/id 3 :person/name "Fred" :person/age 11}
              4 {:person/id 4 :person/name "Bobby" :person/age 55}}
     :relationships
       {:friends {:list/id :friends :list/label "Friends" :list/people [1 2]}
        :enemies
          {:list/id :enemies :list/label "Enemies" :list/people [4 3]}}}))

(defmacro defresolver
  "Like defresolver, but add in a logging step at the beginning and end of the call"
  [resolver-name [env input] pc-map & body]
  (let [inputg (if (symbol? input) input (gensym))
        outputg (gensym)]
    `(pc/defresolver
       ~resolver-name
       [~env ~(cond (map? input) (assoc input :as inputg)
                    (symbol? input) inputg
                    (vector? input) (conj input :as inputg)
                    :else input)]
       ~pc-map
      (let [~outputg ~@body]
        (log/info ~(str resolver-name ": input") ~inputg)
        (log/info ~(str resolver-name ": output: ") ~outputg)
        ~outputg))))

(comment
  (clojure.pprint/pprint
   (macroexpand-1
    '(defresolver crisis-all-resolver
       [env input]
       {::pc/output [{:crisis/list [:crisis/id]}]}
       {:crisis/list (->> '{:find [(pull ?crisis [:crisis/id])]
                            :where [[?crisis :crisis/id _]]}
                          db/q
                          (apply concat))}))))

;; Given :person/id, this can generate the details of a person
(defresolver person-resolver
                [env {:person/keys [id] :as input}]
                {::pc/input #{:person/id}
                 ::pc/output [:person/name :person/age]}
                (get-in @db [:people id]))

;; Given a :list/id, this can generate a list label and the people
;; in that list (but just with their IDs)
(defresolver
  list-resolver
  [env {:list/keys [id] :as input}]
  {::pc/input #{:list/id} ::pc/output [:list/label {:list/people [:person/id]}]}
  (when-let [list (get-in @db [:relationships id])]
    (assoc list
      :list/people (mapv (fn [id] {:person/id id}) (:list/people list)))))

(defresolver friends-resolver
                [env input]
                {::pc/output [{:friends [:list/id]}]}
                {:friends {:list/id :friends}})

(defresolver enemies-resolver
                [env input]
                {::pc/output [{:enemies [:list/id]}]}
                {:enemies {:list/id :enemies}})

(defresolver crisis-resolver
                [env {:crisis/keys [id] :as input}]
                {::pc/input #{:crisis/id}
                 ::pc/output [:crisis/id :crisis/text :crisis/description {:tag/tags [:tag/id]}]}
                (-> (db/q '{:find [(pull ?crisis [:crisis/id :crisis/text :crisis/description
                                                  {:tag/tags [:tag/id]}])]
                            :in [$ ?id]
                            :where [[?crisis :crisis/id ?id]]}
                          [id])
                    ffirst
                    ;; No tags? Make sure there's a placeholder
                    (->> (merge {:tag/tags []}))
                    ;; Change from id to edges
                    (update :tag/tags #(map (fn [{id :tag/id}] [:tag/id id]) %))))

(defresolver crisis-tags-resolver
                [env {id :crisis/id :as input}]
                {::pc/input #{:crisis/id :tag/tags}
                 ::pc/output [:crisis/id :tag/tags]}
                (-> (db/q '{:find [(pull ?crisis [{:tag/tags [:tag/id]}])]
                            :in [$ ?id]
                            :where [[?crisis :crisis/id ?id]]}
                          [id])
                    ffirst))

(defresolver crisis-all-resolver
                [env input]
                {::pc/output [{:crisis/list [:crisis/id]}]}
                {:crisis/list (->> '{:find [(pull ?crisis [:crisis/id])]
                                     :where [[?crisis :crisis/id _]]}
                                   db/q
                                   (apply concat))})

(defresolver tag-resolver
                [env {:tag/keys [id] :as input}]
                {::pc/input #{:tag/id}
                 ::pc/output [:tag/id :tag/name]}
                (-> (db/q '{:find [(pull ?tag [:tag/id :tag/name])]
                            :in [$ ?id]
                            :where [[?tag :tag/id ?id]]}
                          [id])
                    ffirst))
(defresolver tags-all-resolver
  [env input]
  {::pc/output [{:tag/list [:tag/id]}]}
  {:tag/list
   (->> (db/q '{:find [(pull ?tag [:tag/id])]
                :where [[?tag :tag/id _]]})
        (apply concat))})

(def resolvers
  [crisis-all-resolver tags-all-resolver])
