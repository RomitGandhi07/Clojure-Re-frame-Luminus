(ns booksclubwithauth.events.users
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [booksclubwithauth.effects :refer [auth-header]]))

(defn transform-searched-users
  [users]
  (into {}
        (for [user users]
          [(:id user) user])))

(rf/reg-event-fx
  :search-users-success
  (fn [{:keys [db]} [_ path resp]]
    {:dispatch [:stop-loading path]
     :db (assoc db :searched-users (transform-searched-users (:data resp)))}))

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

(rf/reg-event-fx
  :follow-user-success
  (fn [{:keys [db]} [_ path user-id]]
    {:db (assoc-in db [:searched-users user-id :followed] 1)
     :dispatch [:stop-loading path]}))

(rf/reg-event-fx
  :follow-user
  (fn [{:keys [db]} [_ user-id]]
    {:dispatch [:start-loading :follow-user]
     :http-xhrio {:uri (str "api/user/" user-id "/follow")
                  :method :post
                  :params {:follower-id (get-in db [:user :id])}
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers (auth-header db)
                  :on-success [:follow-user-success :follow-user user-id]
                  :on-failure [:http-failure :follow-user]}}))

(rf/reg-event-fx
  :unfollow-user-success
  (fn [{:keys [db]} [_ path user-id]]
    {:db (assoc-in db [:searched-users user-id :followed] 0)
     :dispatch [:stop-loading path]}))

(rf/reg-event-fx
  :unfollow-user
  (fn [{:keys [db]} [_ user-id]]
    {:dispatch [:start-loading :unfollow-user]
     :http-xhrio {:uri (str "api/user/" user-id "/unfollow")
                  :method :delete
                  :params {:follower-id (get-in db [:user :id])}
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers (auth-header db)
                  :on-success [:unfollow-user-success :unfollow-user user-id]
                  :on-failure [:http-failure :unfollow-user]}}))
