(ns booksclubwithauth.middleware
  (:require
    [booksclubwithauth.env :refer [defaults]]
    [clojure.tools.logging :as log]
    [booksclubwithauth.layout :refer [error-page]]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
    [booksclubwithauth.middleware.formats :as formats]
    [muuntaja.middleware :refer [wrap-format wrap-params]]
    [booksclubwithauth.config :refer [env]]
    [ring.middleware.flash :refer [wrap-flash]]
    [buddy.auth.backends :as backends]
    [ring.adapter.undertow.middleware.session :refer [wrap-session]]
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
    [buddy.auth.middleware :refer [wrap-authentication]]
    ;[buddy.auth.accessrules :refer [restrict]]
    [buddy.auth :refer [authenticated?]]
    ;[buddy.auth.backends.session :refer [session-backend]]
    [buddy.sign.jwt :as jwt]
    [clj-time.core :as time])
  )

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title "Invalid anti-forgery token"})}))


(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

;(defn on-error [request response]
;  (error-page
;    {:status 403
;     :title (str "Access to " (:uri request) " is not authorized")}))
;
;(defn wrap-restricted [handler]
;  (restrict handler {:handler authenticated?
;                     :on-error on-error}))
;
;(defn wrap-auth [handler]
;  (let [backend (session-backend)]
;    (-> handler
;        (wrap-authentication backend)
;        (wrap-authorization backend))))

(def jwt-secret "secret")

(def backend (backends/jws {:secret jwt-secret}))

(defn wrap-jwt-authentication
  [handler]
  (wrap-authentication handler backend))

(defn create-token
  [payload]
  (jwt/sign (assoc payload :exp (time/plus (time/now) (time/days 3))) jwt-secret))

(defn auth-middleware
  [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      {:status 401 :body {:error "Unauthorized User"}})))


(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-jwt-authentication
      wrap-flash
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (dissoc :session)))
      wrap-internal-error))