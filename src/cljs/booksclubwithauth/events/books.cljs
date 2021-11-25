(ns booksclubwithauth.events.books
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [booksclubwithauth.effects :refer [auth-header]]))

(rf/reg-event-fx
  :my-read-books-success
  (fn [cofx [_ path resp]]
    {:dispatch [:stop-loading path]
     :db (assoc (:db cofx) :read-books (:data resp))}))


(rf/reg-event-fx
  :my-read-books
  (fn [{:keys [db]}]
    {:dispatch [:start-loading :my-books]
     :db (dissoc db :read-books)
     :http-xhrio {:uri (str "api/user/" (get-in db [:user :id]) "/book")
                  :method :get
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers (auth-header db)
                  :on-success [:my-read-books-success :my-books]
                  :on-failure [:http-failure :my-books]}}))

(rf/reg-event-fx
  :delete-book-success
  (fn [cofx [_ path resp]]
    {:db (assoc (:db cofx) :toast {:success (:message resp)})
     :dispatch-n (list
                   [:stop-loading path]
                   [:my-read-books])}))

(rf/reg-event-fx
  :delete-book
  (fn [{:keys [db]} [_ id]]
    {:dispatch [:start-loading :delete-book]
     :http-xhrio {:uri (str "api/user/" (get-in db [:user :id]) "/book/" id)
                  :method :delete
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers (auth-header db)
                  :on-success [:delete-book-success :delete-book]
                  :on-failure [:http-failure :delete-book]}}))

(rf/reg-event-fx
  :add-book-success
  (fn [cofx [_ path resp]]
    {:dispatch [:stop-loading path]
     :db (assoc (:db cofx) :toast {:success (:message resp)})
     :navigate! [:my-books]}))

(rf/reg-event-fx
  :add-book
  (fn [{:keys [db]} [_ data]]
    {:dispatch [:start-loading :add-book]
     :http-xhrio {:uri (str "api/user/" (get-in db [:user :id]) "/book")
                  :method :post
                  :params data
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers (auth-header db)
                  :on-success [:add-book-success :add-book]
                  :on-failure [:http-failure :add-book]}}))

(rf/reg-event-fx
  :fetch-book-details-success
  (fn [{:keys [db]} [_ path resp]]
    {:dispatch [:stop-loading path]
     :db (assoc db :update-book (:data resp))}))

(rf/reg-event-fx
  :fetch-book-details
  (fn [{:keys [db]} [_ book-id]]
    {:db (dissoc db :update-book)
     :dispatch [:start-loading :fetch-book]
     :http-xhrio {:uri (str "api/user/" (get-in db [:user :id]) "/book/" book-id)
                  :method :get
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers (auth-header db)
                  :on-success [:fetch-book-details-success :fetch-book]
                  :on-failure [:http-failure :fetch-book]}}))

(rf/reg-event-fx
  :update-book-success
  (fn [cofx [_ path resp]]
    {:dispatch [:stop-loading path]
     :db (assoc (:db cofx) :toast {:success (:message resp)})
     :navigate! [:my-books]}))

(rf/reg-event-fx
  :update-book
  (fn [{:keys [db]} [_ data book-id]]
    {:dispatch [:start-loading :update-book]
     :http-xhrio {:uri (str "api/user/" (get-in db [:user :id]) "/book/" book-id)
                  :method :put
                  :params data
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers (auth-header db)
                  :on-success [:update-book-success :update-book]
                  :on-failure [:http-failure :update-book]}}))

