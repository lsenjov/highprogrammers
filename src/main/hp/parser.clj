(ns hp.parser
  (:require [hp.resolvers]
            [hp.mutations]
            [hp.db]
            [com.wsscode.pathom.connect.datomic.client :refer [client-config]]
            [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.connect :as pc]
            [com.wsscode.pathom.connect.datomic :as pcd]
            [taoensso.timbre :as log]))

(defn resolvers [] [(hp.resolvers/resolvers) (hp.mutations/mutations)])

(def whitelist-attributes
  (->> hp.db/all-schema
       (map :db/ident)
       set))
(def ident-attributes
  (->> hp.db/all-schema
       (filter #(= :db.type/ref (get % :db/valueType)))
       (map :db/ident)
       set))

(def debug-plugin
  {::pc/wrap-resolve (fn [resolve]
                       (fn [env input]
                         (log/debug "debug-plugin: input:" #_env input)
                         (let [ret (resolve env input)]
                           (log/debug "debug-plugin: output:" ret)
                           ret)))})

(def pathom-parser
  (p/parser
    {::p/env {::p/reader [p/map-reader pc/reader3 pc/open-ident-reader
                          p/env-placeholder-reader]
              ::pc/mutation-join-globals [:tempids]}
     ::p/mutate pc/mutate
     ::p/plugins [debug-plugin (p/post-process-parser-plugin p/elide-not-found)
                  (pc/connect-plugin {::pc/register (resolvers)})
                  (pcd/datomic-connect-plugin
                    (assoc client-config
                      ::pcd/conn hp.db/conn
                      ::pcd/whitelist whitelist-attributes
                      ::pcd/ident-attributes ident-attributes))
                  p/error-handler-plugin p/trace-plugin]}))

(defn api-parser [query] (log/info "Process" query) (pathom-parser {} query))

(comment
  (api-parser [:crisis/list
               [:crisis/list
                {:crisis/ids [:crisis/id :crisis/text :crisis/description
                              {:tag/tags [:tag/id :tag/name]}
                              {[:tag/list '_] [:tag/id :tag/name]}]}]])
  (api-parser [{:crisis/list [:crisis/list {:crisis/ids [:crisis/id]}]}])
  (api-parser [{:friends [:list/id {:list/people [:person/name]}]}
               {:enemies [:list/id {:list/people [:person/name]}]}])
  (api-parser [{:friends [:list/id {:list/people [:person/name]}]}])
  (api-parser [{[:crisis/id "first"] [:crisis/id {:tag/tags [:tag/id]}]}])
  (api-parser [{[:crisis/id "second"] [:crisis/id :crisis/text
                                       :crisis/description
                                       {:tag/tags [:tag/id]}]}])
  (api-parser [{:crisis/list [:crisis/id :crisis/text :crisis/description
                              {:tag/tags [:tag/id :tag/name]} :tag/tags->e]}])
  (api-parser [:crisis/list :crisis/id {:crisis/list [:crisis/id]}])
  (api-parser [:crisis/list
               {:crisis/id [:crisis/id {:tag/tags [:tag/id :tag/name]}]}])
  (api-parser [{:crisis/list [:crisis/list :crisis/id]}])
  (api-parser [{[:crisis/list "all"] [:crisis/list {:crisis/id [:crisis/id]}]}])
  (api-parser [{[:crisis/list "all"] [:crisis/list :crisis/id]}])
  (api-parser [{[:tag/id "classic"] [:tag/id :tag/name]}])
  (api-parser [{:tag/list [:tag/id :tag/name]}])
  (api-parser [{:tag/tags []}])
  (api-parser [{[:crisis/id "first"] [:crisis/id
                                      {:tag/tags [:tag/id :tag/name]}]}]))
