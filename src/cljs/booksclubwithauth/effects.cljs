(ns booksclubwithauth.effects
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]))

;;dispatchers
(defonce books-club-ls-user-key "books-club-user-token")  ;; localstorage key


;(def set-user-interceptor [                                 ;;(rf/path :user-demo)        ;; `:user` path within `db`, rather than the full `db`.
;                           (rf/after set-user-ls) ;; write user to localstore (after)
;                           rf/trim-v])            ;; removes first (event id) element from the event vec

(defn auth-header
  "Get user token and format for API authorization"
  [db]
  (when-let [token (:user-secret db)]
    [:Authorization (str "Token " token)]))

;; -- cofx Registrations  -----------------------------------------------------
(rf/reg-cofx
  :local-store-user
  (fn [cofx _]
    (assoc cofx :local-store-user
                      (some->> (.getItem js/localStorage books-club-ls-user-key)
                               (cljs.reader/read-string)))))
;-----------------------------------------------------------
; Events

(rf/reg-event-fx
  :initialise-db
  [(rf/inject-cofx :local-store-user)]
  (fn [{:keys [local-store-user]} _]
    (if local-store-user
      {:db (assoc {} :user-secret local-store-user)
       :http-xhrio {:uri "/api/verifyToken"
                    :method :get
                    :response-format (ajax/json-response-format {:keywords? true})
                    :headers {:Authorization (str "Token " local-store-user)}
                    :on-success [:token-verify-success]
                    :on-failure [:token-verify-failure]}}
      {:db {}
       :dispatch [:token-verify-failure]})))

(rf/reg-fx
  :set-token-ls
  (fn [token]
    (.setItem js/localStorage books-club-ls-user-key (str token))))

(rf/reg-fx
  :remove-token-ls
  (fn [_]
    (.removeItem js/localStorage books-club-ls-user-key)))

(rf/reg-event-db
  :token-verify-success
  (fn [db [_ resp]]
    (assoc db :user (:data resp))))

(rf/reg-event-fx
  :token-verify-failure
  (fn [cofx]
    {:navigate! [:login]}))

(rf/reg-event-fx
  :check-authentication
  (fn [{:keys [db]}]
    (if (empty? (:user db))
      {:navigate! [:login]})))

(rf/reg-event-db
  :start-loading
  (fn [db [_ path]]
    (assoc-in db [:loading path] true)))

(rf/reg-event-db
  :stop-loading
  (fn [db [_ path]]
    (assoc-in db [:loading path] false)))

(rf/reg-event-db
  :clear-error
  (fn [db]
    (dissoc db :error :error-response)))

(rf/reg-event-db
  :clear-toast-notification
  (fn [db]
    (dissoc db :toast)))

(rf/reg-event-fx
  :http-failure
  (fn [{:keys [db]} [_ path resp]]
    (let [error (get-in resp [:response :error])]
      {:db (-> db
               (assoc-in [:error path] error)
               (assoc :error-response resp)
               (assoc :toast {:error (if (map? error)
                                        "Something went wrong..."
                                        error)}))
       :dispatch [:stop-loading path]})))


(rf/reg-event-db
  :common/navigate
  (fn [db [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :common/route new-match))))

(rf/reg-fx
  :common/navigate-fx!
  (fn [[k & [params query]]]
    (rfe/push-state k params query)))

(rf/reg-fx
  :navigate!
  (fn [route]
    (apply rfe/push-state route)))

(rf/reg-event-fx
  :navigate-login!
  (fn [db]
    {:navigate! [:login]}))

(rf/reg-event-fx
  :common/navigate!
  (fn [_ [_ url-key params query]]
    {:common/navigate-fx! [url-key params query]}))

(rf/reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))

(rf/reg-event-fx
  :fetch-docs
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "/docs"
                  :response-format (ajax/raw-response-format)
                  :on-success       [:set-docs]}}))

(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (assoc db :common/error error)))

(rf/reg-event-fx
  :page/init-home
  (fn [_ _]
    {:dispatch [:fetch-docs]}))
