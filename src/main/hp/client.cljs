(ns hp.client
  (:require [hp.application :refer [app]]
            [hp.ui :as ui]
            [hp.ui.crisis :as ui.crisis]
            [hp.ui.tag :as ui.tag]
            [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.data-fetch :as df]))

(defn ^:export init
  "Shadow-cljs sets this up to be our entry-point function. See shadow-cljs.edn `:init-fn` in the modules of the main build."
  []
  (app/mount! app ui/Root "app")
  ;; (df/load! app :friends ui/PersonList)
  ;; (df/load! app :enemies ui/PersonList)
  (df/load! app :crisis/list ui.crisis/Crisis)
  (df/load! app :tag/list ui.tag/Tag)
  (js/console.log "Loaded"))

(defn ^:export refresh
  "During development, shadow-cljs will call this on every hot reload of source. See shadow-cljs.edn"
  []
  ;; re-mounting will cause forced UI refresh, update internals, etc.
  (app/mount! app ui/Root "app")
  (js/console.log "Hot reload"))
