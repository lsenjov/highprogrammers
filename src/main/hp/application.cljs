(ns hp.application
  (:require [com.fulcrologic.fulcro.components :as comp]
            [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.networking.http-remote :as http]
            [com.fulcrologic.fulcro.networking.websockets :as fws]
            [taoensso.timbre :as log]))

;; We define this down the page, because this works on the app
(declare push-handler*)
(defn push-handler [x] (push-handler* x))

(defonce app
         (app/fulcro-app {:remotes {:remote (fws/fulcro-websocket-remote
                                              {:csrf-token "bad-csrf"
                                               :push-handler push-handler})
                                    ;:remote-http (http/fulcro-http-remote {})
                                   }}))

(defn add-wssync-to-symbol
  [sym]
  {:pre [(namespace sym)]}
  (let [sym-ns (namespace sym)
        sym-name (name sym)]
    (symbol sym-ns (str sym-name "-wssync"))))
(defn add-wssync-to-mutation
  [mut]
  ;; We assume the first item is a symbol
  (cons (add-wssync-to-symbol (first mut)) (rest mut)))
(comment (add-wssync-to-mutation '(asdf/qwer [15 5])))
(defn apply-mutations
  [mutations]
  (->> mutations
       (mapv add-wssync-to-mutation)
       (comp/transact! app)))
(defn push-handler*
  [{:keys [topic msg]}]
  (case topic
    :state-mutations (apply-mutations msg)
    (log/debug "No matching push-handler clause for: " topic)))
