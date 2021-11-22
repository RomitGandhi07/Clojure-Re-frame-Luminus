(ns booksclubwithauth.validation
  (:require [struct.core :as st]))

;; ADD - UPDATE BOOK
(def book-schema
  "Book Schema which contains validations of add/update book"
  [[:title
    [st/required :message "Title is required"]
    [st/string :message "Title must be string"]]

   [:author
    [st/required :message "Author is required"]
    [st/string :message "Author must be string"]]

   [:rating
    [st/required :message "Rating is required"]]

   [:image_url
    [st/string  :message "Image URL must be string"]]

   [:description
    [st/string  :message "Description must be string"]]])

(defn add-update-book-validation
  [params]
  (first (st/validate params book-schema)))

;; USER REGISTRATION

(def user-registration-schema
  "User registration Schema which contains validations of user registration data"
  [[:name
    [st/required :message "Name is required"]
    [st/string :message "Name must be string"]]

   [:email
    [st/required :message "Email is required"]
    [st/email :message "Email must be valid"]]

   [:password
    [st/required :message "Password is required"]
    [st/string :message "Password must be string"]]

   [:profile_pic
    [st/string :message "Profile pic must be string"]]])


(defn validate-user-registration-data
  [data]
  (first (st/validate data user-registration-schema)))

;; USER LOGIN

(def user-login-schema
  "User registration Schema which contains validations of user registration data"
  [[:email
    [st/required :message "Email is required"]
    [st/email :message "Email must be valid"]]

   [:password
    [st/required :message "Password is required"]
    [st/string :message "Password must be string"]]])

(defn validate-user-login-data
  [data]
  (first (st/validate data user-login-schema)))