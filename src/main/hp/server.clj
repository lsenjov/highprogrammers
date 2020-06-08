(ns hp.server
  (:require
    [hp.parser :refer [api-parser]]
    [org.httpkit.server :as http]
    [com.fulcrologic.fulcro.server.api-middleware :as server]
    [com.fulcrologic.fulcro.networking.websockets :as fws]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.not-modified :refer [wrap-not-modified]]
    [ring.middleware.resource :refer [wrap-resource]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
    [taoensso.timbre :as log]
    [clojure.core.async :as async]))

(def ^:private not-found-handler
  (fn [req]
    {:status  404
     :headers {"Content-Type" "text/plain"}
     :body    "Not Found"}))

(def middleware
  (-> not-found-handler
    (server/wrap-api {:uri    "/api"
                      :parser api-parser})
    (server/wrap-transit-params)
    (server/wrap-transit-response)
    (wrap-resource "public")
    wrap-content-type))

(defonce
  ^{:doc "We store the server stop in here"}
  stop-fn (atom nil))
(defonce
  ^{:doc "When we create the websocket we throw it in this atom, so we can use it for other things"}
  *websocket (atom nil))

(defn query-parser*
  "Figures out what we're doing, proxies onwards"
  [env query]
  (let [websockets (:websockets env)
        user (:cid env)
        all-other-users (-> env :websockets :connected-uids deref :any
                            ;; Don't return to sender
                            (disj user))]
    (async/go
      (let [mutations (vec (filter #(-> % first namespace (= "hp.mutations")) query))]
        (when (pos? (count mutations))
          (log/info "Pushing to " all-other-users)
          (doseq [uid all-other-users]
            (.push websockets uid :state-mutations query)))))
    (api-parser query)))
(defn query-parser
  "Used for a redirect when doing RAD"
  [env query]
  (query-parser* env query))

(defn start []
  (let [websockets
        (fws/start!
          (fws/make-websockets
            query-parser
            {:http-server-adapter (get-sch-adapter)
             :parser-accepts-env? true
             :sente-options {:csrf-token-fn (constantly "bad-csrf")}}))
        middleware
        (-> not-found-handler
            (server/wrap-api {:uri    "/api"
                              :parser api-parser})
            (fws/wrap-api websockets)
            wrap-keyword-params
            wrap-params
            (server/wrap-transit-params)
            (server/wrap-transit-response)
            (wrap-resource "public")
            wrap-content-type
            wrap-not-modified)
        server-stop-fn
        (http/run-server middleware {:host "0.0.0.0" :port 3000})]
    (reset! *websocket websockets)
    (reset! stop-fn
            (fn []
              (fws/stop! websockets)
              (server-stop-fn)))))

(defn stop []
  (when @stop-fn
    (@stop-fn)
    (reset! stop-fn nil)))
