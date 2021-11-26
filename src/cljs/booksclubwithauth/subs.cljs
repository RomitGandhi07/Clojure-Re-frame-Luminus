(ns booksclubwithauth.subs
  (:require
    [re-frame.core :as rf]))

(def default-profile-img "https://t3.ftcdn.net/jpg/03/46/83/96/360_F_346839683_6nAPzbhpSkIpb8pmAwufkC7c5eD7wYws.jpg")

(rf/reg-sub
  :common/route
  (fn [db _]
    (-> db :common/route)))

(rf/reg-sub
  :common/page-id
  :<- [:common/route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :common/page
  :<- [:common/route]
  (fn [route _]
    (-> route :data :view)))

(rf/reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))

(rf/reg-sub
  :error
  (fn [db]
    (get db :error)))

(rf/reg-sub
  :user
  (fn [db]
    (get db :user)))

(rf/reg-sub
  :user-name
  (fn []
    (rf/subscribe [:user]))
  (fn [user]
    (get user :name)))

(rf/reg-sub
  :user-profile-image
  (fn []
    (rf/subscribe [:user]))
  (fn [user]
    (let [profile-image (:profile_pic user)]
      (if (empty? profile-image)
        default-profile-img
        profile-image))))


;; Update Books
(rf/reg-sub
  :update-book
  (fn [db]
    (:update-book db)))


(rf/reg-event-db
  :books/my-read-books-success
  (fn [db [_ data]]
    (assoc db :read-books (:data data))))

(rf/reg-sub
  :books/read-books
  (fn [db]
    (:read-books db)))

(rf/reg-sub
  :books/my-read-books-ids
  (fn []
    (rf/subscribe [:books/read-books]))
  (fn [books]
    (mapv (fn [book] (:id book)) books)))

(rf/reg-sub
  :books/read-book
  (fn []
    (rf/subscribe [:books/read-books]))
  (fn [books [_ id]]
    (first (filter (fn [book] (= id (:id book))) books))))

; ---------------------------
; LOADING

(rf/reg-sub
  :loading
  (fn [db]
    (:loading db)))

(rf/reg-sub
  :loading/my-books
  (fn []
    (rf/subscribe [:loading]))
  (fn [loading]
    (:my-books loading)))

; ---------------------------
; ERRORS

(rf/reg-sub
  :error
  (fn [db]
    (:error db)))

(rf/reg-sub
  :error/login
  (fn []
    (rf/subscribe [:error]))
  (fn [error]
    (:login error)))

(rf/reg-sub
  :error/registration
  (fn []
    (rf/subscribe [:error]))
  (fn [error]
    (:registration error)))

(rf/reg-sub
  :error/add-update-book
  (fn []
    (rf/subscribe [:error]))
  (fn [error]
    (or (:add-book error) (:update-book error))))

(rf/reg-sub
  :error-response
  (fn [db]
    (:error-response db)))

(rf/reg-sub
  :searched-users
  (fn [db]
    (:searched-users db)))

(rf/reg-sub
  :searched-users-ids
  (fn []
    (rf/subscribe [:searched-users]))
  (fn [users]
    (keys users)))

(rf/reg-sub
  :searched-user-info
  (fn []
    (rf/subscribe [:searched-users]))
  (fn [users [_ id]]
    (get users id)))

(rf/reg-sub
  :searched-user-info-follow
  (fn []
    (rf/subscribe [:searched-users]))
  (fn [users [_ id]]
    (not= (get-in users [id :followed]) 0)))

(rf/reg-sub
  :toast
  (fn [db]
    (:toast db)))