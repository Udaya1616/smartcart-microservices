-- SmartCart database setup script
-- Run this file before starting the services:
-- mysql -u root -p < docs/database-setup.sql

CREATE DATABASE IF NOT EXISTS smartcart_users
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS smartcart_products
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS smartcart_orders
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smartcart_users;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

USE smartcart_products;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500) NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_products_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

USE smartcart_orders;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_orders_user_id (user_id),
    KEY idx_orders_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Optional sample products.
-- Admin user is not inserted here because user-service creates it automatically
-- with username 'admin' and password 'admin123' when the service starts.
INSERT INTO smartcart_products.products (name, description, price, quantity)
SELECT 'Wireless Mouse', 'Bluetooth wireless mouse', 499.00, 25
WHERE NOT EXISTS (
    SELECT 1 FROM smartcart_products.products WHERE name = 'Wireless Mouse'
);

INSERT INTO smartcart_products.products (name, description, price, quantity)
SELECT 'Mechanical Keyboard', 'USB mechanical keyboard', 2499.00, 10
WHERE NOT EXISTS (
    SELECT 1 FROM smartcart_products.products WHERE name = 'Mechanical Keyboard'
);
