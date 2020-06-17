(ns juxt.jdbc)

(defn query [q conn]
  (with-open [stmt (.createStatement conn)
              rs (.executeQuery stmt q)]
    (->> rs resultset-seq (into []))))
