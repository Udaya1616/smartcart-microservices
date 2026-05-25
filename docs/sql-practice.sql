CREATE DATABASE IF NOT EXISTS smartcart_users;
CREATE DATABASE IF NOT EXISTS smartcart_products;
CREATE DATABASE IF NOT EXISTS smartcart_orders;

USE smartcart_users;
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50)
);

USE smartcart_products;
CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200),
    description VARCHAR(500),
    price DECIMAL(10,2),
    quantity INT
);

USE smartcart_orders;
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    product_id BIGINT,
    quantity INT,
    total_amount DECIMAL(10,2)
);

SELECT o.id, u.username, p.name AS product_name, o.quantity, o.total_amount
FROM smartcart_orders.orders o
JOIN smartcart_users.users u ON u.id = o.user_id
JOIN smartcart_products.products p ON p.id = o.product_id;

SELECT user_id, COUNT(*) AS order_count, SUM(total_amount) AS total_spent
FROM smartcart_orders.orders
GROUP BY user_id;

SELECT product_id, SUM(quantity) AS total_quantity_ordered
FROM smartcart_orders.orders
GROUP BY product_id
ORDER BY total_quantity_ordered DESC;

SELECT *
FROM smartcart_products.products
WHERE quantity > 0 AND price BETWEEN 100 AND 1000
ORDER BY price;
