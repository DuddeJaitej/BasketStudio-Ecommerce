CREATE DATABASE IF NOT EXISTS basket_studio CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE basket_studio;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NULL,
    phone VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    provider VARCHAR(30) NOT NULL DEFAULT 'LOCAL',
    provider_subject VARCHAR(190) NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id VARCHAR(80) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(40) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    image VARCHAR(255) NOT NULL,
    tagline VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_product_price CHECK (price > 0)
);

CREATE TABLE orders (
    id VARCHAR(20) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PLACED',
    payment_method VARCHAR(40) NOT NULL,
    address_name VARCHAR(120) NOT NULL,
    address_phone VARCHAR(20) NOT NULL,
    address_line VARCHAR(255) NOT NULL,
    address_city VARCHAR(100) NOT NULL,
    address_state VARCHAR(100) NOT NULL,
    address_pincode VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id VARCHAR(20) NOT NULL,
    product_id VARCHAR(80) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT chk_item_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_orders_user_created ON orders(user_id, created_at);
CREATE INDEX idx_products_category ON products(category);

INSERT INTO products (id, name, category, price, image, tagline) VALUES
('tomato', 'Tomatoes', 'Vegetables', 27, 'wp9998853-tomato-4k-wallpapers.jpg', 'Vine-ripened and bright'),
('onions', 'Onions', 'Vegetables', 35, 'wp7253201-onions-wallpapers.jpg', 'Kitchen essential'),
('garlic', 'Garlic', 'Vegetables', 40, 'wp4309869-garlic-wallpapers.jpg', 'Aromatic cloves'),
('chilli', 'Green Chilli', 'Vegetables', 50, 'wp9327759-green-chilli-wallpapers.jpg', 'Fresh and fiery'),
('spinach', 'Baby Spinach', 'Vegetables', 15, 'wp3145595-fresh-spinach-wallpapers.jpg', 'Tender leafy greens'),
('cabbage', 'Cabbage', 'Vegetables', 38, 'wp4159429-cabbage-wallpapers.jpg', 'Crisp and crunchy'),
('potato', 'Potatoes', 'Vegetables', 32, 'wp2473648-potatoes-wallpapers.jpg', 'Everyday comfort'),
('bitter-gourd', 'Bitter Gourd', 'Vegetables', 40, 'wp10276571-bitter-gourd-wallpapers.jpg', 'Garden-fresh goodness'),
('apple', 'Royal Apples', 'Fruits', 27, 'apple.jpeg', 'Sweet orchard crunch'),
('orange', 'Nagpur Oranges', 'Fruits', 35, 'orange.jpeg', 'Citrus sunshine'),
('banana', 'Bananas', 'Fruits', 40, 'banana.jpeg', 'Naturally energy-rich'),
('guava', 'Guavas', 'Fruits', 50, 'guava.jpeg', 'Fragrant and juicy'),
('dragon', 'Dragon Fruit', 'Fruits', 15, 'dragon.jpeg', 'A tropical favorite'),
('pineapple', 'Pineapple', 'Fruits', 38, 'pineapple.jpg', 'Golden and tangy'),
('avocado', 'Avocado', 'Fruits', 32, 'avacado.jpg', 'Silky and nourishing'),
('strawberries', 'Strawberries', 'Fruits', 40, 'strawberries.jpg', 'Ruby-red sweetness');
