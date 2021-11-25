(ns booksclubwithauth.handler.book
  (:require
    [ring.util.http-response :as response]
    [struct.core :as st]
    [booksclubwithauth.db.core :as db]
    [booksclubwithauth.handler.common :refer [db-success?]]
    [booksclubwithauth.validation :refer [validate-add-update-book-data]]
    [failjure.core :as f]))

(defn validate-book
  "Returns failure if the data isn't following the book-schema

  If failure then it contains
  {:title \"Title is required\"} ... like object"
  [params]
  (let [erros (validate-add-update-book-data params)]
    (when (some? erros)
      (f/fail erros))))

(defn add-book
  "Returns a response/[ok] for a successful insertion for book

  First it will check whether the input data is following the book-schema then it will insert in DB

  If both the things succeed then response/[ok] otherwise response/[bad-request]"
  [{{user-id :user-id} :path-params
    data :params}]
  (f/attempt-all [_ (validate-book data)
                  _ (db-success? (db/create-book! (assoc data :user_id user-id)))]
                 (response/ok {:message "Book successfully added..."})
                 (f/when-failed [e]
                                (response/bad-request {:error (:message e "Something went wrong...")}))))

(defn update-book
  "Returns a response/[ok] for a successful update for book

  First it will check whether the input data is following the book-schema then it will update in DB

  If both the things succeed then response/[ok] otherwise response/[bad-request]"
  [{data :params
    {book-id :book-id
     user-id :user-id} :path-params}]
  (f/attempt-all [_ (validate-book data)
                  _ (db-success? (db/update-book! (merge data {:id book-id
                                                               :user_id user-id})))]
                 (response/ok {:message "Book successfully updated..."})
                 (f/when-failed [e]
                                (response/bad-request {:error (:message e "Something went wrong...")}))))

(defn get-books
  "Returns response/[ok] if fetch all books query succeed otherwise response/[bad-request]"
  [{{user-id :user-id} :path-params}]
  (f/if-let-failed? [books (db-success? (db/get-user-books! {:user_id user-id}))]
                    (response/bad-request {:error "Something went wrong..."})
                    (response/ok {:data books})))

(defn delete-book
  "Returns response/[ok] if delete book query succeed otherwise response/[bad-request]"
  [{{book-id :book-id
     user-id :user-id} :path-params}]
  (f/if-let-failed? [_ (db-success? (db/delete-book! {:id book-id
                                                      :user_id user-id}))]
                    (response/not-found {:error "Book with given id not found..."})
                    (response/ok {:message "Book successfully deleted..."})))

(defn get-book-by-id
  [{{book-id :book-id
     user-id :user-id} :path-params}]
  (f/if-let-failed? [data (db-success? (db/get-book-by-id! {:id book-id
                                                            :user_id user-id}))]
                    (response/not-found {:error "Book with given id not found..."})
                    (response/ok {:message "Book successfully fetched..."
                                  :data data})))

;(defn get-book-by-id
;  [request]
;  (response/not-found {:error "Book with given id not found..."}))