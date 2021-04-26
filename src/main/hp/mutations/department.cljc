(ns hp.mutations.department
  (:require #?(:clj [com.wsscode.pathom.connect :as pc :refer [defmutation]]
               :cljs [com.fulcrologic.fulcro.mutations :as pc :refer
                      [defmutation]])
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [taoensso.timbre :as log]
            #?(:clj [hp.db])))