(ns booksclubwithauth.events.books
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [booksclubwithauth.effects :refer [auth-header]]))

(rf/reg-event-db
  :my-read-books-success
  (fn [db [_ data]]
    (-> db
        (assoc :read-books (:data data))
        (assoc-in [:loading :books] false))))


(rf/reg-event-fx
  :my-read-books
  (fn [cofx]
    {:db (-> (:db cofx)
             (assoc-in [:loading :books] true)
             (dissoc :read-books))
     :http-xhrio {:uri (str "api/user/" (get-in cofx [:db :user :id]) "/book")
                  :method :get
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers (auth-header (:db cofx))
                  :on-success [:my-read-books-success]
                  :on-failure [:http-failure]}}))

(rf/reg-event-fx
  :delete-book-success
  (fn [cofx]
    {:dispatch [:my-read-books]}))

(rf/reg-event-fx
  :delete-book
  (fn [cofx [_ id]]
    {:http-xhrio {:uri (str "api/user/" (get-in cofx [:db :user :id]) "/book/" id)
                  :method :delete
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers (auth-header (:db cofx))
                  :on-success [:delete-book-success]
                  :on-failure [:http-failure]}}))

(rf/reg-event-fx
  :add-book-success
  (fn [cofx]
    {:db (assoc (:db cofx) :error nil)
     :navigate! [:my-books]}))

(rf/reg-event-fx
  :add-book
  (fn [cofx [_ data]]
    {:http-xhrio {:uri (str "api/user/" (get-in cofx [:db :user :id]) "/book")
                  :method :post
                  :params data
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers (auth-header (:db cofx))
                  :on-success [:add-book-success]
                  :on-failure [:http-failure]}}))

(rf/reg-event-db
  :fetch-book-details-success
  (fn [db [_ resp]]
    (assoc db :update-book (:data resp))))

(rf/reg-event-fx
  :fetch-book-details
  (fn [cofx [_ book-id]]
    {:http-xhrio {:uri (str "api/user/" (get-in cofx [:db :user :id]) "/book/" book-id)
                  :method :get
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers (auth-header (:db cofx))
                  :on-success [:fetch-book-details-success]
                  :on-failure [:http-failure]}}))

(rf/reg-event-fx
  :update-book-success
  (fn [cofx]
    {:db (dissoc (:db cofx) :update-book :error)
     :navigate! [:my-books]}))

(rf/reg-event-fx
  :update-book
  (fn [cofx [_ data book-id]]
    {:http-xhrio {:uri (str "api/user/" (get-in cofx [:db :user :id]) "/book/" book-id)
                  :method :put
                  :params data
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers (auth-header (:db cofx))
                  :on-success [:update-book-success]
                  :on-failure [:http-failure]}}))

