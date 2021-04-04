(ns hp.parser
  (:require [hp.resolvers]
            [hp.mutations]
            [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.connect :as pc]
            [taoensso.timbre :as log]))

(def resolvers [hp.resolvers/resolvers hp.mutations/mutations])

(def pathom-parser
  (p/parser {::p/env {::p/reader [p/map-reader pc/reader2 pc/ident-reader
                                  pc/index-reader]
                      ::pc/mutation-join-globals [:tempids]}
             ::p/mutate pc/mutate
             ::p/plugins [(pc/connect-plugin {::pc/register resolvers})
                          p/error-handler-plugin]}))

(defn api-parser [query] (log/info "Process" query) (pathom-parser {} query))

(comment (api-parser [{:friends [:list/id {:list/people [:person/name]}]}
                      {:enemies [:list/id {:list/people [:person/name]}]}])
         (api-parser [{:friends [:list/id {:list/people [:person/name]}]}])
         (api-parser [{[:crisis/id "first"] [:crisis/text]}])
         (api-parser [{:crisis/list [:crisis/id :crisis/text
                                     :crisis/description]}])
         (api-parser [{:crisis/list [:crisis/id {:tag/tags [:tag/id :tag/name]}]}])
         (api-parser [:crisis/list])
         (api-parser [{[:tag/id "classic"] [:tag/id :tag/name]}])
         (api-parser [{:tag/list [:tag/id :tag/name]}]))
