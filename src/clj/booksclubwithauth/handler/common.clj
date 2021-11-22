(ns booksclubwithauth.handler.common
  (:require [failjure.core :as f]
            [buddy.core.hash :as hash]
            [buddy.core.codecs :refer [bytes->hex]]))

;; :one or :1 = one row as a hash-map or nil otherwise
;; :many or :* = many rows as a vector of hash-maps or empty seq otherwise
;; :affected or :n = number of rows affected (inserted/updated/deleted) or zero otherwise
(defmulti db-success? (fn [db-result & message] (type db-result)))

(defmethod db-success? Integer [db-result & message]
  (if-not (zero? db-result)
    db-result
    (f/fail (or (first message) "The operation did not succeed as expected."))))

(defmethod db-success? clojure.lang.LazySeq [db-result & message]
  (if (some? db-result)
    db-result
    (f/fail (or (first message) "The operation did not succeed as expected."))))

(defmethod db-success? clojure.lang.PersistentVector [db-result & message]
  (if (some? db-result)
    db-result
    (f/fail (or (first message) "The operation didn't produce any result"))))

(defmethod db-success? clojure.lang.PersistentArrayMap [db-result & message]
  (if (some? db-result)
    db-result
    (f/fail (or (first message) "The operation didn't produce any result"))))

(defmethod db-success? nil [db-result & message]
  (f/fail (or (first message) "The operation didn't produce any result")))

(defn encrypt-password
  [password]
  (-> (hash/sha256 password)
      (bytes->hex)))