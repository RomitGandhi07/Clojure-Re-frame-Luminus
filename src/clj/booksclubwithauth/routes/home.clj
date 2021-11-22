(ns booksclubwithauth.routes.home
  (:require
   [booksclubwithauth.layout :as layout]
   [booksclubwithauth.db.core :as db]
   [clojure.java.io :as io]
   [booksclubwithauth.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]
   [booksclubwithauth.handler.book :as book]
   [booksclubwithauth.handler.users :as user]))

(defn home-page [request]
  (layout/render request "home.html"))

(defn home-routes []
  [""
   {:middleware [
                 ;; middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]

   ["/api/register" {:post user/registration}]

   ["/api/login" {:post user/login}]

   ["/api/verifyToken" {:middleware [middleware/auth-middleware]
                        :get user/verifyToken}]

   ["/api/user/:user-id/book" {:middleware [middleware/auth-middleware]
                 :get book/get-books
                 :post book/add-book}]

   ["/api/user/:user-id/book/:book-id" {:middleware [middleware/auth-middleware]
                                        :get book/get-book-by-id
                                        :put book/update-book
                                        :delete book/delete-book}]])