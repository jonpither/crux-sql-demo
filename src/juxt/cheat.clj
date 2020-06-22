(ns juxt.cheat
  (:require [crux.api :as crux]))

(comment
  (crux/q (crux/db (crux-node)) '{:find [?name] :where [[?e :name ?name]]})

  ;; clear out
  (let [eids (crux/q (crux/db (crux-node)) '{:find [?e] :where [[?e :name ?name]]})]
    (crux/submit-tx (crux-node) (mapv #(vector :crux.tx/delete %) (map first eids)))))
