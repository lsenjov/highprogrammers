(ns hp.application
  (:require
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.networking.http-remote :as http]
    [com.fulcrologic.fulcro.networking.websockets :as fws]
    [taoensso.timbre :as log]
    ))

;; We define this down the page, because this works on the app
(declare push-handler*)
(defn push-handler
  [x]
  (push-handler* x))

(defonce app
  (app/fulcro-app
    {:remotes
     {:remote (fws/fulcro-websocket-remote
                {:csrf-token "bad-csrf"
                 :push-handler push-handler})
      ;:remote-http (http/fulcro-http-remote {})
      }}))

(defn apply-mutations
  [mutations]
  (comp/transact! app mutations))
(defn push-handler*
  [{:keys [topic msg]}]
  (case topic
    :state-mutations (apply-mutations msg)
    (log/debug "No matching push-handler clause for: " topic)
    ))
