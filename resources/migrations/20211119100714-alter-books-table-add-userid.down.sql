ALTER TABLE books
    DROP COLUMN user_id,
    DROP FOREIGN KEY fk_books_userid;