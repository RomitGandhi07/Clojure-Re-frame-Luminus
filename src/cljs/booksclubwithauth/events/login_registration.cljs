(ns booksclubwithauth.events.login-registration
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [booksclubwithauth.effects :refer [set-user-ls remove-user-ls]]))


(rf/reg-event-fx
  :user-registration
  (fn [cofx [_ data]]
    {:http-xhrio {:uri "/api/register"
                  :method :post
                  :params data
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:login-registration-success]
                  :on-failure [:http-failure]}}))

(rf/reg-event-fx
  :user-login
  (fn [cofx [_ data]]
    {:http-xhrio {:uri "/api/login"
                  :method :post
                  :params data
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:login-registration-success]
                  :on-failure [:http-failure]}}))

(rf/reg-event-fx
  :logout
  (fn [db]
    (remove-user-ls)
    {:db (-> db
             (dissoc :user)
             (dissoc :user-secret))
     :navigate! [:login]}))

(rf/reg-event-fx
  :login-registration-success
  (fn [cofx [_ resp]]
    (set-user-ls resp)
    {:db (-> (:db cofx)
             (assoc :user-secret (get-in resp [:data :token]))
             (assoc :user (:data resp))
             (dissoc :error))
     :navigate! [:my-books]}))