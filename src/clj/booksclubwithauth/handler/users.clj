(ns booksclubwithauth.handler.users
  (:require
    [ring.util.http-response :as response]
    [struct.core :as st]
    [booksclubwithauth.db.core :as db]
    [booksclubwithauth.handler.common :refer [db-success? encrypt-password error-response send-email]]
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
      (f/fail {:status 400
               :message errors}))))

(defn check-user-login-data
  "Returns failure if the data isn't following the user login schema

  If failure then it contains
  {:email \"Email is required\"} ... like object"
  [data]
  (let [errors (validate-user-login-data data)]
    (when (some? errors)
      (f/fail {:status 400
               :message errors}))))

(defn user-exist?
  [email]
  (if (not (empty? (db/get-user-by-email! {:email email})))
    (f/fail {:status 400
             :message "User exist with same email address"})))

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
  (try
    (f/attempt-all [_ (check-user-registration-data data)
                    _ (user-exist? (:email data))
                    _ (db-success? (db/create-user! (update data :password encrypt-password)))
                    user (db-success? (db/get-user-by-email! {:email (:email data)}))
                    token (create-jwt-token user)
                    _ (send-email {"to" (:email user)
                                   "name" (:name user)
                                   "registration?" true})]
                   (response/ok {:message "User successfully registered..."
                                 :data (user-data-to-send user token)})
                   (f/when-failed [e]
                                  (error-response e)))
    (catch Exception e
      (response/internal-server-error {:error "Something went wrong... Please try again"}))))

(defn login
  [{data :params}]
  (try
    (f/attempt-all [_ (check-user-login-data data)
                    user (db-success? (db/get-user-by-email-password! (update data :password encrypt-password))
                                      {:status 404
                                       :message "User with given email & password not found..."})
                    token (create-jwt-token user)]
                   (response/ok {:message "Logged in successfully..."
                                 :data (user-data-to-send user token)})
                   (f/when-failed [e]
                                  (error-response e)))
    (catch Exception e
      (response/internal-server-error {:error "Something went wrong... Please try again"}))))

(defn verifyToken
  [{data :identity}]
  (try
    (f/if-let-failed? [user (db-success? (db/get-user-by-id! {:id (:user-id data)}))]
                      (response/bad-request {:error "Something went wrong..."})
                      (response/ok {:message "Details successfully fetched..."
                                    :data user}))
    (catch Exception e
      (response/internal-server-error {:error "Something went wrong... Please try again"}))))

(defn search-users
  [{{name "name"} :query-params :as req}]
  (try
    (f/if-let-failed? [users (db-success? (db/search-users! {:id (get-in req [:identity :user-id])
                                                             :name name}))]
                      (response/bad-request {:error "Something went wrong..."})
                      (response/ok {:message "Users successfully fetched..."
                                    :data users}))
    (catch Exception e
      (response/internal-server-error {:error "Something went wrong... Please try again"}))))

(defn user-followed
  [data]
  (not (empty? (db/user-followed? data))))

(defn already-followed?
  [data]
  (when (user-followed data)
    (f/fail {:status 400
             :message "User already followed..."})))

(defn not-following?
  [data]
  (when-not (user-followed data)
    (f/fail {:status 400
             :message "You are not following the user..."})))


(defn follow-user
  [{{user-id :user-id} :path-params
    {follower-id :follower-id} :params}]
  (try
    (f/attempt-all [data {:user_id user-id
                          :follower_id follower-id}
                    _ (already-followed? data)
                    _ (db-success? (db/follow-user! data))]
                   (response/ok {:message "User successfully followed..."})
                   (f/when-failed [e]
                                  (error-response e)))
    (catch Exception e
      (response/internal-server-error {:error "Something went wrong... Please try again"}))))

(defn unfollow-user
  [{{user-id :user-id} :path-params
    {follower-id :follower-id} :params}]
  (try
    (f/attempt-all [data {:user_id user-id
                          :follower_id follower-id}
                    _ (not-following? data)
                    _ (db-success? (db/unfollow-user! data))]
                   (response/ok {:message "User successfully unfollowed..."})
                   (f/when-failed [e]
                                  (error-response e)))
    (catch Exception e
      (response/internal-server-error {:error "Something went wrong... Please try again"}))))

(defn email
  [{{to :to} :params}]
  (send-email to)
  (response/ok {:message "Ok"}))
