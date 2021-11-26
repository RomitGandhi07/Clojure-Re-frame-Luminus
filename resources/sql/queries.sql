-- :name create-book! :! :n
-- :doc creates a new book
INSERT INTO books
(title,author,rating,image_url,description, user_id)
VALUES (:title, :author, :rating, :image_url, :description, :user_id)

-- :name update-book! :! :n
-- :doc updates an existing book
UPDATE books
SET title = :title, author = :author, rating = :rating, image_url = :image_url, description = :description
WHERE id = :id AND user_id = :user_id

-- :name get-user-books! :? :*
-- :doc retrieves all books of particular user
SELECT * FROM books where user_id = :user_id ORDER BY id DESC

-- :name get-book-by-id! :? :1
-- :doc retrieves book by id
SELECT * FROM books where id = :id AND user_id = :user_id

-- :name delete-book! :! :n
-- :doc deletes a book given the id
DELETE FROM books
WHERE id = :id AND user_id = :user_id


-- :name create-user! :! :n
-- :doc creates a new user
INSERT INTO users
(name,email,password,profile_pic)
VALUES (:name, :email, :password, :profile_pic)

-- :name get-user-by-id! :? :1
-- :doc retrieves user by id
SELECT * FROM users WHERE id = :id

-- :name get-user-by-email! :? :1
-- :doc retrieves user by email
SELECT * FROM users WHERE email = :email

-- :name get-user-by-email-password! :? :1
-- :doc retrieves user by email and password
SELECT * FROM users WHERE email = :email AND password = :password


-- :name search-users! :? :*
-- :doc retrieves user by name
SELECT A.id,A.name,A.email,A.profile_pic, IF(B.id IS NULL, false, true) as followed FROM users as A
LEFT JOIN follow as B ON A.id = B.user_id
WHERE A.id != :id AND A.name LIKE CONCAT('%',:name,'%') LIMIT 50

-- :name follow-user! :! :n
-- :doc follow user
INSERT INTO follow
(user_id, follower_id)
VALUES (:user_id,:follower_id)

-- :name unfollow-user! :! :n
-- :doc unfollow user
DELETE FROM follow
WHERE user_id = :user_id AND follower_id = :follower_id

-- :name user-followed? :? :1
-- :doc Check user has followed or not
SELECT * FROM follow WHERE user_id = :user_id AND follower_id = :follower_id