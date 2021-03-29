(ns hp.workspaces.main
  (:require [com.fulcrologic.fulcro.components :as fp]
            [fulcro.client.localized-dom :as dom]
            [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
            [nubank.workspaces.lib.fulcro-portal :as f.portal]
            [fulcro.client.mutations :as fm]
            ;[hp.ui]
  ))
(defonce init (ws/mount))

(defn element
  [name props & children]
  (apply js/React.createElement name (clj->js props) children))

(fp/defsc BasicDemo
          [this {:ui/keys [counter] :as props}]
          {:initial-state (fn [_] (println "Initial state") {:ui/counter 0})
           :ident (fn [] [::id "singleton"])
           :query [:ui/counter]}
          (dom/div (println "Props:" props)
                   (str "Fulcro counter demo [" counter "]")
                   (dom/button
                     {:onClick #(fm/set-value! this :ui/counter (inc counter))}
                     "+")))

(ws/defcard basic-demo-card
            (ct.fulcro/fulcro-card {::ct.fulcro/root BasicDemo
                                    ::ct.fulcro/wrap-root? true}))
