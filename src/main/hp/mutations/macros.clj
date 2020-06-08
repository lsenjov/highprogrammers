(ns hp.mutations.macros
  (:require [com.fulcrologic.fulcro.mutations]))
(defn add-wssync-to-symbol
  [sym]
  (let [sym-ns (namespace sym)
        sym-name (name sym)]
    (if sym-ns
      (symbol sym-ns (str sym-name "-wssync"))
      (symbol (str sym-name "-wssync")))))
(defmacro def-wssync-mutation
  "Use like a defmutation, but also declares a second version with -wssync on the end
  without any remotes"
  [mutation-name & body]
  (let [no-remotes-body (remove #(and (list? %) (= 'remote (first %))) body)
        wssync-name (add-wssync-to-symbol mutation-name)]
    `(do
       (com.fulcrologic.fulcro.mutations/defmutation ~mutation-name ~@body)
       (com.fulcrologic.fulcro.mutations/defmutation ~wssync-name ~@no-remotes-body))))

(comment
  (macroexpand-1 '(def-wssync-mutation delete-person [env {list-id   :list/id
                                                  person-id :person/id}]
                    (remote [env] 0))))
