CREATE DATABASE IF NOT EXISTS online_bookstore
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE online_bookstore;

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS books (
    book_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100),
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    image_url VARCHAR(500) DEFAULT '/static/images/default.jpg',
    category_id INT,
    stock INT DEFAULT 100,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT books_ibfk_1 FOREIGN KEY (category_id) REFERENCES categories(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cart (
    cart_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    quantity INT DEFAULT 1,
    CONSTRAINT cart_ibfk_1 FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT cart_ibfk_2 FOREIGN KEY (book_id) REFERENCES books(book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS book_categories (
    book_id INT NOT NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (book_id, category_id),
    CONSTRAINT book_categories_ibfk_1 FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    CONSTRAINT book_categories_ibfk_2 FOREIGN KEY (category_id) REFERENCES categories(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) UNIQUE,
    user_id INT NOT NULL,
    total_price DECIMAL(10, 2),
    status VARCHAR(20) DEFAULT 'Pending',
    address VARCHAR(500),
    payment_method VARCHAR(50) DEFAULT 'Online Payment',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT orders_ibfk_1 FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    book_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT order_items_ibfk_1 FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT order_items_ibfk_2 FOREIGN KEY (book_id) REFERENCES books(book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS favorites (
    favorite_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY favorites_user_book (user_id, book_id),
    CONSTRAINT favorites_ibfk_1 FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT favorites_ibfk_2 FOREIGN KEY (book_id) REFERENCES books(book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS browsing_history (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    view_count INT DEFAULT 1,
    last_viewed DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY history_user_book (user_id, book_id),
    CONSTRAINT browsing_history_ibfk_1 FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT browsing_history_ibfk_2 FOREIGN KEY (book_id) REFERENCES books(book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO users (user_id, username, password, email, role)
VALUES
    (1, 'admin', 'admin123', 'admin@bookstore.com', 'admin')
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    email = VALUES(email),
    role = VALUES(role);

INSERT INTO categories (category_id, category_name, description)
VALUES
    (1, 'Literature', 'Classic and contemporary fiction'),
    (2, 'Computer Science', 'Programming, algorithms, AI'),
    (3, 'Economics', 'Business, finance, management'),
    (4, 'History', 'Historical events and biographies'),
    (5, 'Science', 'Physics, chemistry, biology'),
    (6, 'Design', 'Design systems, typography, visual thinking'),
    (7, 'Psychology', 'Cognition, behavior, decision-making'),
    (8, 'Philosophy', 'Classical and modern philosophy'),
    (9, 'Art', 'Art history, criticism, visual culture'),
    (10, 'Travel', 'Cities, journeys, nature writing'),
    (11, 'Education', 'Learning methods and teaching practice'),
    (12, 'Biography', 'Lives, memoirs, and personal history')
ON DUPLICATE KEY UPDATE
    category_name = VALUES(category_name),
    description = VALUES(description);

INSERT INTO books (book_id, title, author, price, description, image_url, category_id, stock)
VALUES
    (1, 'To Live', 'Yu Hua', 35.00, 'A moving novel about ordinary people and resilience.', '/static/images/default.jpg', 1, 196),
    (2, 'One Hundred Years of Solitude', 'Gabriel Garcia Marquez', 55.00, 'A landmark work of magical realism.', '/static/images/default.jpg', 1, 150),
    (3, 'Core Java', 'Cay Horstmann', 128.00, 'A practical and detailed Java programming guide.', '/static/images/default.jpg', 2, 80),
    (4, 'Computer Systems: A Programmers View', 'Randal E. Bryant', 139.00, 'A classic systems programming and computer architecture text.', '/static/images/default.jpg', 2, 60),
    (5, 'Principles of Economics', 'N. Gregory Mankiw', 88.00, 'An introductory economics text for business and management study.', '/static/images/default.jpg', 3, 100),
    (6, 'The Ming Dynasty', 'Dang Nian Ming Yue', 198.00, 'A readable history of the Ming dynasty.', '/static/images/default.jpg', 4, 119),
    (7, 'A Brief History of Time', 'Stephen Hawking', 45.00, 'A concise introduction to cosmology and modern physics.', '/static/images/default.jpg', 5, 90),
    (8, 'Clean Code', 'Robert C. Martin', 68.00, 'Guidance for writing readable, maintainable software.', '/static/images/default.jpg', 2, 110),
    (9, 'Sapiens', 'Yuval Noah Harari', 72.00, 'A broad history of humankind.', '/static/images/default.jpg', 4, 130),
    (10, 'The Art of War', 'Sun Tzu', 25.00, 'A classic work on strategy and decision-making.', '/static/images/default.jpg', 4, 200),
    (11, 'Designing Interfaces', 'Jenifer Tidwell', 96.00, 'Patterns for thoughtful interaction design and interface decisions.', '/static/images/default.jpg', 6, 72),
    (12, 'The Design of Everyday Things', 'Don Norman', 62.00, 'A clear introduction to usability, affordances, and product thinking.', '/static/images/default.jpg', 6, 88),
    (13, 'Thinking, Fast and Slow', 'Daniel Kahneman', 79.00, 'A landmark book on judgment, bias, and human decision-making.', '/static/images/default.jpg', 7, 95),
    (14, 'Influence', 'Robert Cialdini', 58.00, 'A practical study of persuasion and social psychology.', '/static/images/default.jpg', 7, 85),
    (15, 'The Republic', 'Plato', 42.00, 'A foundational work of political philosophy and ethics.', '/static/images/default.jpg', 8, 64),
    (16, 'Meditations', 'Marcus Aurelius', 36.00, 'Stoic reflections on discipline, clarity, and inner life.', '/static/images/default.jpg', 8, 120),
    (17, 'Ways of Seeing', 'John Berger', 48.00, 'Short essays on art, images, and how we learn to look.', '/static/images/default.jpg', 9, 77),
    (18, 'The Story of Art', 'E. H. Gombrich', 118.00, 'A widely read introduction to art history across periods and cultures.', '/static/images/default.jpg', 9, 52),
    (19, 'The Great Railway Bazaar', 'Paul Theroux', 54.00, 'A vivid travel narrative across Europe and Asia by train.', '/static/images/default.jpg', 10, 69),
    (20, 'Into the Wild', 'Jon Krakauer', 46.00, 'A spare account of travel, wilderness, risk, and solitude.', '/static/images/default.jpg', 10, 81),
    (21, 'How Learning Works', 'Susan A. Ambrose', 86.00, 'Research-based principles for better learning and teaching.', '/static/images/default.jpg', 11, 58),
    (22, 'Make It Stick', 'Peter C. Brown', 49.00, 'A concise book on durable learning, memory, and practice.', '/static/images/default.jpg', 11, 102),
    (23, 'Steve Jobs', 'Walter Isaacson', 78.00, 'A biography about product taste, focus, and creative leadership.', '/static/images/default.jpg', 12, 73),
    (24, 'Becoming', 'Michelle Obama', 68.00, 'A memoir about identity, public life, and personal growth.', '/static/images/default.jpg', 12, 66)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    author = VALUES(author),
    price = VALUES(price),
    description = VALUES(description),
    image_url = VALUES(image_url),
    category_id = VALUES(category_id),
    stock = VALUES(stock);

INSERT IGNORE INTO book_categories (book_id, category_id)
SELECT book_id, category_id FROM books WHERE category_id IS NOT NULL;

INSERT IGNORE INTO book_categories (book_id, category_id)
VALUES
    (2, 8),
    (7, 8),
    (9, 7),
    (12, 7),
    (13, 3),
    (16, 12),
    (17, 6),
    (20, 12),
    (21, 7),
    (23, 3);
