(ns booksclubwithauth.handler.users
  (:require
    [ring.util.http-response :as response]
    [struct.core :as st]
    [booksclubwithauth.db.core :as db]
    [booksclubwithauth.handler.common :refer [db-success? encrypt-password]]
    [failjure.core :as f]
    [booksclubwithauth.middleware :refer [create-token]]))

(def user-registration-schema
  "User registration Schema which contains validations of user registration data"
  [[:name
    [st/required :message "Name is required"]
    [st/string :message "Name must be string"]]

   [:email
    [st/required :message "Email is required"]
    [st/email :message "Email must be valid"]]

   [:password
    [st/required :message "Password is required"]
    [st/string :message "Password must be string"]]

   [:profile_pic
    [st/string :message "Profile pic must be string"]]])

(def user-login-schema
  "User registration Schema which contains validations of user registration data"
  [[:email
    [st/required :message "Email is required"]
    [st/email :message "Email must be valid"]]

   [:password
    [st/required :message "Password is required"]
    [st/string :message "Password must be string"]]])

(defn validate-user-registration-data
  "Returns failure if the data isn't following the user-registration-schema

  If failure then it contains
  {:name \"Name is required\"} ... like object"
  [data]
  (let [errors (first (st/validate data user-registration-schema))]
    (when (some? errors)
      (f/fail errors))))

(defn validate-user-login-data
  "Returns failure if the data isn't following the user login schema

  If failure then it contains
  {:email \"Email is required\"} ... like object"
  [data]
  (let [errors (first (st/validate data user-login-schema))]
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
  (f/attempt-all [_ (validate-user-registration-data data)
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
  (f/attempt-all [_ (validate-user-login-data data)
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
