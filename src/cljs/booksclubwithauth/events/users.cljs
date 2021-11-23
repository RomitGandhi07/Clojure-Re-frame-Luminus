(ns booksclubwithauth.events.users
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [booksclubwithauth.effects :refer [auth-header]]))

(rf/reg-event-fx
  :search-users-success
  (fn [{:keys [db]} [_ path resp]]
    {:dispatch [:stop-loading path]
     :db (assoc db :searched-users (:data resp))}))

(rf/reg-event-fx
  :search-users
  (fn [cofx [_ name]]
    {:dispatch [:start-loading :search-users]
     :http-xhrio {:uri (str "api/user/search?name=" name)
                  :method :get
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers (auth-header (:db cofx))
                  :on-success [:search-users-success :search-users]
                  :on-failure [:http-failure :search-users]}}))
