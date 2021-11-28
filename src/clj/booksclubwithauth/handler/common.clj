(ns booksclubwithauth.handler.common
  (:require [failjure.core :as f]
            [buddy.core.hash :as hash]
            [buddy.core.codecs :refer [bytes->hex]]
            [ring.util.http-response :as response]
            [postal.core :refer [send-message]]
            [taoensso.carmine :as car :refer [wcar]]
            [clojure.data.json :as json]))

;; :one or :1 = one row as a hash-map or nil otherwise
;; :many or :* = many rows as a vector of hash-maps or empty seq otherwise
;; :affected or :n = number of rows affected (inserted/updated/deleted) or zero otherwise
(defmulti db-success? (fn [db-result & error] (type db-result)))

(defmethod db-success? Integer [db-result & error]
  (if-not (zero? db-result)
    db-result
    (f/fail {:status (or (:status (first error)) 400)
             :message (or (:message (first error)) "The operation did not succeed as expected.")})))

(defmethod db-success? clojure.lang.LazySeq [db-result & error]
  (if (some? db-result)
    db-result
    (f/fail {:status (or (:status (first error)) 400)
             :message (or (:message (first error)) "The operation did not succeed as expected.")})))

(defmethod db-success? clojure.lang.PersistentVector [db-result & error]
  (if (some? db-result)
    db-result
    (f/fail {:status (or (:status (first error)) 400)
             :message (or (:message (first error)) "The operation didn't produce any result")})))

(defmethod db-success? clojure.lang.PersistentArrayMap [db-result & error]
  (if (some? db-result)
    db-result
    (f/fail {:status (or (:status (first error)) 400)
             :message (or (:message (first error)) "The operation didn't produce any result")})))

(defmethod db-success? nil [db-result & error]
  (f/fail {:status (or (:status (first error)) 400)
           :message (or (:message (first error)) "The operation didn't produce any result")}))

(defn encrypt-password
  [password]
  (-> (hash/sha256 password)
      (bytes->hex)))

(defn error-response
  [{e :message}]
  (cond
    (= (:status e) 400)
    (response/bad-request {:error (:message e "Bad request...")})
    (= (:status e) 404)
    (response/not-found {:error (:message e "Not Found...")})
    (= (:status e) 500)
    (response/internal-server-error {:error (:message e "Something went wrong...")})
    :else
    (response/bad-request {:error (:message e "Something went wrong...")})))

(defn send-email
  [to]
  (send-message {:host "smtp.gmail.com"
                 :user "democera@gmail.com"
                 :pass "Password@123"
                 :port 587
                 :tls true}
                {:from "democera@gmail.com"
                 :to to
                 :subject "Hi!"
                 :body "Test."}))

(def server1-conn {:pool {} :spec {:uri "redis://redistogo:4f77e548d5905b50fc71f55cb2c2a6e5@sole.redistogo.com:9441/"}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn send-email
  [data]
  (let [c-data (json/write-str data
                               :key-fn #(str %))]
    (println c-data)
    (wcar* (car/publish "email" c-data))))