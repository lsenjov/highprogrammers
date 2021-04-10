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
    `(pc/defresolver ~resolver-name
                     [~env
                      ~(cond (map? input) (assoc input :as inputg)
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

(defresolver crisis-all-resolver
             [env input]
             {::pc/output [{:crisis/list [:crisis/id]}]}
             {:crisis/list (->> '{:find [(pull ?crisis [:crisis/id])]
                                  :where [[?crisis :crisis/id _]]}
                                db/q
                                (apply concat))})
(defresolver tags-all-resolver
             [env input]
             {::pc/output [{:tag/list [:tag/id]}]}
             {:tag/list (->> (db/q '{:find [(pull ?tag [:tag/id])]
                                     :where [[?tag :tag/id _]]})
                             (apply concat))})

(def resolvers [crisis-all-resolver tags-all-resolver])
