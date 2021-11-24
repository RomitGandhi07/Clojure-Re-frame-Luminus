(ns booksclubwithauth.handler.book
  (:require
    [ring.util.http-response :as response]
    [struct.core :as st]
    [booksclubwithauth.db.core :as db]
    [booksclubwithauth.handler.common :refer [db-success?]]
    [failjure.core :as f]))

(def book-schema
  "Book Schema which contains validations of book"
  [[:title
   [st/required :message "Title is required"]
   [st/string :message "Title must be string"]]
   
   [:author
    [st/required :message "Author is required"]
    [st/string :message "Author must be string"]]
   
   [:rating
    [st/required :message "Rating is required"]
    ]
   
   [:image_url
    [st/string  :message "Image URL must be string"]]
   
   [:description
    [st/string  :message "Description must be string"]]])

(defn validate-book
  "Returns failure if the data isn't following the book-schema

  If failure then it contains
  {:title \"Title is required\"} ... like object"
  [params]
  (let [erros (first (st/validate params book-schema))]
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

(comment
  (def db {:user          {:id    123
                           :email "abc@gmail.com"}
           :shopping-cart {}
           :products      {1256 {:id          1256
                                 :name        "Car"
                                 :description ""
                                 :price       200}
                           434  {:id          434
                                 :name        "Truck"
                                 :description ".123"}}})

  ;;Add/replace values in deeply nested level

  (assoc-in db [:a :b] 1)

  (assoc-in db [:products 564] {:id   564
                                :name "Truck"})

  ;; Take what's there and modify it
  (update-in db [:products 1256 :name] str "...For sale")

  (rf/reg-event-db
    :increment-price
    (fn [db [_ product-id]]
      (update-in db [:products product-id :price] inc)))

  (let [product-id 434]
    (update-in db [:products product-id :price] (fnil inc 0)))


  (-> []
      (conj 3)
      (conj 4))

  (-> nil
      ((fnil conj []) 3)
      (conj 4))

  ;; Get value from deeply nested structure
  (rf/reg-sub
    :product-name
    (fn [db [_ product-id]]
      (get-in db [:products product-id :name])))

  (def a {:title {:value ""
                  :touched? true
                  :changed? true
                  :error ""}

          :author {:value ""
                  :touched? true
                  :changed? true
                  :error ""}

          :description {:value ""
                  :touched? true
                  :changed? true
                  :error ""}})

  (into {} (for [i a]
    [(first i) (:value (second i))]))

  (def a {:a 1 :b 2})

  (assoc a :c 3)

  (println a)

  (defn demo [a]
    (+ a 5))

  )