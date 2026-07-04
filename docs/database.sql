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

CREATE TABLE IF NOT EXISTS orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    total_price DECIMAL(10, 2),
    status VARCHAR(20) DEFAULT 'Pending',
    address VARCHAR(500),
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
    (5, 'Science', 'Physics, chemistry, biology')
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
    (10, 'The Art of War', 'Sun Tzu', 25.00, 'A classic work on strategy and decision-making.', '/static/images/default.jpg', 4, 200)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    author = VALUES(author),
    price = VALUES(price),
    description = VALUES(description),
    image_url = VALUES(image_url),
    category_id = VALUES(category_id),
    stock = VALUES(stock);
