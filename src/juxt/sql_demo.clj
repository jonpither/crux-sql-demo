(ns juxt.sql-demo
  (:require [clojure.tools.namespace.repl :as ctn]
            [integrant.repl.state :refer [system]]
            [integrant.repl :as ir :refer [clear go suspend resume halt reset reset-all]]
            [crux.api :as crux]
            crux.kv.rocksdb
            crux.standalone
            [integrant.core :as i])
  (:import crux.api.ICruxAPI))

(ctn/disable-reload!)

(defonce node (crux/start-node {:crux.node/topology ['crux.standalone/topology
                                                     'crux.kv.rocksdb/kv-store]
                                :crux.kv/db-dir "db"
                                :crux.standalone/event-log-kv-store 'crux.kv.rocksdb/kv
                                :crux.standalone/event-log-dir "event-log"}))

(defmethod i/init-key :node [_ node-opts]
  (crux/start-node node-opts))

(defmethod i/halt-key! :node [_ ^ICruxAPI node]
  (.close node))

(defn crux-node []
  (:node system))

;; I want a minimal reset
;; do everything via the REPL - have a cheat sheet
