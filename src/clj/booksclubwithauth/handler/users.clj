(ns booksclubwithauth.handler.users
  (:require
    [ring.util.http-response :as response]
    [struct.core :as st]
    [booksclubwithauth.db.core :as db]
    [booksclubwithauth.handler.common :refer [db-success? encrypt-password]]
    [failjure.core :as f]
    [booksclubwithauth.middleware :refer [create-token]]
    [booksclubwithauth.validation :refer [validate-user-registration-data validate-user-login-data]]))

(defn check-user-registration-data
  "Returns failure if the data isn't following the user-registration-schema

  If failure then it contains
  {:name \"Name is required\"} ... like object"
  [data]
  (let [errors (validate-user-registration-data data)]
    (when (some? errors)
      (f/fail errors))))

(defn check-user-login-data
  "Returns failure if the data isn't following the user login schema

  If failure then it contains
  {:email \"Email is required\"} ... like object"
  [data]
  (let [errors (validate-user-login-data data)]
    (when (some? errors)
      (f/fail errors))))

(defn user-exist?
  [email]
  (if (not (empty? (db/get-user-by-email! {:email email})))
    (f/fail "User exist with same email address")))(f/fail "User exist with same email address")

(defn user-data-to-send
  [user token]
  (-> user
      (dissoc :password)
      (assoc :token token)))

(defn create-jwt-token
  [user]
  (create-token {:email (:email user)
                 :user-id (:id user)
                 :name (:name user)}))

(defn registration
  [{data :params}]
  (f/attempt-all [_ (check-user-registration-data data)
                  _ (user-exist? (:email data))
                  _ (db-success? (db/create-user! (update data :password encrypt-password)))
                  user (db-success? (db/get-user-by-email! {:email (:email data)}))
                  token (create-jwt-token user)]
                 (response/ok {:message "User successfully registered..."
                               :data (user-data-to-send user token)})
                 (f/when-failed [e]
                                (response/bad-request {:error (:message e "Something went wrong...")}))))

(defn login
  [{data :params}]
  (f/attempt-all [_ (check-user-login-data data)
                  user (db-success? (db/get-user-by-email-password! (update data :password encrypt-password))
                                    "User not found with given email address and password")
                  token (create-jwt-token user)]
                 (response/ok {:message "Logged in successfully..."
                               :data (user-data-to-send user token)})
                 (f/when-failed [e]
                                (response/bad-request {:error (:message e "Something went wrong...")}))))

(defn verifyToken
  [{data :identity}]
  (f/if-let-failed? [user (db-success? (db/get-user-by-id! {:id (:user-id data)}))]
                    (response/bad-request {:error "Something went wrong..."})
                    (response/ok {:message "Details successfully fetched..."
                                  :data user})))

(defn search-users
  [{{name "name"} :query-params :as req}]
  (f/if-let-failed? [users (db-success? (db/search-users! {:id (get-in req [:identity :user-id])
                                                           :name name}))]
                    (response/bad-request {:error "Something went wrong..."})
                    (response/ok {:message "Users successfully fetched..."
                                  :data users})))
