-- Run this in MySQL Workbench as an administrator account.
CREATE DATABASE IF NOT EXISTS basket_studio CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'basket_app'@'localhost' IDENTIFIED BY 'ChangeThisStrongPassword!';
ALTER USER 'basket_app'@'localhost' IDENTIFIED BY 'ChangeThisStrongPassword!';
GRANT ALL PRIVILEGES ON basket_studio.* TO 'basket_app'@'localhost';
FLUSH PRIVILEGES;
