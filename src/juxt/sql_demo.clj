(ns juxt.sql-demo
  (:require [clojure.tools.namespace.repl :as ctn]
            [integrant.repl.state :refer [system]]
            [integrant.repl :as ir :refer [clear go suspend resume halt reset reset-all]]
            [crux.api :as crux]
            crux.kv.rocksdb
            crux.standalone
            crux.calcite
            [juxt.jdbc :refer [query]]
            [integrant.core :as i])
  (:import crux.api.ICruxAPI
           java.util.UUID))

;; ---------- Crux Config ----------- ;;



(def config {:node {:crux.node/topology ['crux.standalone/topology
                                         'crux.kv.rocksdb/kv-store
                                         'crux.calcite/module]

                    :crux.kv/db-dir "db"
                    :crux.standalone/event-log-kv-store 'crux.kv.rocksdb/kv
                    :crux.standalone/event-log-dir "event-log"}})



;; ---------- Integrant ----------- ;;

(ctn/disable-reload!)

(defmethod i/init-key :node [_ node-opts]
  (crux/start-node node-opts))

(defmethod i/halt-key! :node [_ ^ICruxAPI node]
  (.close node))

(ir/set-prep! (fn [] config))

(defn crux-node []
  (:node system))





;; ---------- Test People ----------- ;;

(defn random-person []
  {:crux.db/id (UUID/randomUUID)
   :name (rand-nth ["Ivan" "Slata" "Sergei" "Antonina" "Yuri" "Dmitry" "Maria" "Denis"])
   :last-name (rand-nth ["Ivanov" "Petrov" "Sidorov" "Kovalev" "Kuznetsov" "Voronoi"])
   :sex (rand-nth [:male :female])
   :age (rand-int 100)
   :salary (rand-int 100000)})

(comment
  (let [node (crux-node)]
    (doseq [p (take 100 (repeatedly random-person))]
      (let [tx (crux/submit-tx node [[:crux.tx/put p]])]
        (crux/await-tx node tx))))








  ;; ---------- Table Schema ----------- ;;

  (crux/submit-tx (crux-node) [[:crux.tx/put {:crux.db/id :crux.sql.schema/person
                                              :crux.sql.table/name "person"
                                              :crux.sql.table/query '{:find [?id ?name ?age]
                                                                      :where [[?id :name ?name]
                                                                              [?id :age ?age]]}
                                              :crux.sql.table/columns '{?id :keyword, ?name :varchar, ?age :bigint}}]])
















  ;; ---------- JDBC Query ----------- ;;
  (def conn (crux.calcite/jdbc-connection (crux-node)))
  (take 5 (query "SELECT NAME FROM PERSON" conn))












  ;; ---------- Update Table Schema ----------- ;;
  (crux/submit-tx (crux-node) [[:crux.tx/put {:crux.db/id :crux.sql.schema/employee
                                              :crux.sql.table/name "employee"
                                              :crux.sql.table/query '{:find [?id ?name ?salary]
                                                                      :where [[?id :name ?name]
                                                                              [?id :salary ?salary]]}
                                              :crux.sql.table/columns '{?id :keyword, ?name :varchar, ?salary :bigint}}]]))
