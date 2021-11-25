(ns booksclubwithauth.events.login-registration
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]))


(rf/reg-event-fx
  :user-registration
  (fn [cofx [_ data]]
    {:dispatch [:start-loading :registration]
     :http-xhrio {:uri "/api/register"
                  :method :post
                  :params data
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:login-registration-success :registration]
                  :on-failure [:http-failure :registration]}}))

(rf/reg-event-fx
  :user-login
  (fn [cofx [_ data]]
    {:dispatch [:start-loading :login]
     :http-xhrio {:uri "/api/login"
                  :method :post
                  :params data
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:login-registration-success :login]
                  :on-failure [:http-failure :login]}}))

(rf/reg-event-fx
  :logout
  (fn [db]
    {:db (-> db
             (dissoc :user)
             (dissoc :user-secret))
     :remove-token-ls {}
     :navigate! [:login]}))

(rf/reg-event-fx
  :login-registration-success
  (fn [cofx [_ path resp]]
    {:dispatch [:stop-loading path]
     :db (-> (:db cofx)
             (assoc :user-secret (get-in resp [:data :token]))
             (assoc :user (:data resp))
             (dissoc :error))
     :set-token-ls (get-in resp [:data :token])
     :navigate! [:my-books]}))