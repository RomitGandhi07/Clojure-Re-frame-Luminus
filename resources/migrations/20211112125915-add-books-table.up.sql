CREATE TABLE books
(id INT AUTO_INCREMENT PRIMARY KEY,
 title VARCHAR(1000) NOT NULL,
 author VARCHAR(1000) NOT NULL,
 image_url TEXT,
 rating INT NOT NULL,
 description TEXT);
