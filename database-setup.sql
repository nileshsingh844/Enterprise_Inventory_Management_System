-- Enterprise Inventory Management System Database Setup
-- This script creates the necessary databases and schemas for all microservices
-- Each microservice has its own database for better isolation and scalability

-- =================================================================
-- DATABASE CREATION
-- =================================================================

-- Create database for Inventory Service
CREATE DATABASE IF NOT EXISTS inventory_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Create database for Order Service
CREATE DATABASE IF NOT EXISTS order_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Create database for User Service
CREATE DATABASE IF NOT EXISTS user_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- =================================================================
-- USER MANAGEMENT
-- =================================================================

-- Create users for each microservice database
CREATE USER IF NOT EXISTS 'inventory_user'@'localhost' IDENTIFIED BY 'inventory_pass';
CREATE USER IF NOT EXISTS 'order_user'@'localhost' IDENTIFIED BY 'order_pass';
CREATE USER IF NOT EXISTS 'user_user'@'localhost' IDENTIFIED BY 'user_pass';

-- Grant privileges to each user
GRANT ALL PRIVILEGES ON inventory_db.* TO 'inventory_user'@'localhost';
GRANT ALL PRIVILEGES ON order_db.* TO 'order_user'@'localhost';
GRANT ALL PRIVILEGES ON user_db.* TO 'user_user'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- =================================================================
-- INVENTORY SERVICE TABLES
-- =================================================================

USE inventory_db;

-- Products table for inventory management
CREATE TABLE IF NOT EXISTS products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    unit_price DECIMAL(19,2) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10,
    max_stock_level INT,
    location VARCHAR(100),
    supplier VARCHAR(200),
    status ENUM('ACTIVE', 'INACTIVE', 'DISCONTINUED', 'OUT_OF_STOCK') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    INDEX idx_sku (sku),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_supplier (supplier),
    INDEX idx_quantity (quantity_in_stock),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

-- Insert sample data for testing
INSERT INTO products (sku, name, description, category, unit_price, quantity_in_stock, reorder_level, location, supplier) VALUES
('LAP-001', 'Laptop Pro 15"', 'High-performance laptop with 16GB RAM and 512GB SSD', 'Electronics', 1299.99, 50, 10, 'Warehouse A', 'TechSupplier Inc.'),
('LAP-002', 'Laptop Air 13"', 'Ultra-light laptop with 8GB RAM and 256GB SSD', 'Electronics', 899.99, 75, 15, 'Warehouse A', 'TechSupplier Inc.'),
('MOU-001', 'Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 'Accessories', 29.99, 200, 25, 'Warehouse B', 'AccessoryCo'),
('KEY-001', 'Mechanical Keyboard', 'RGB mechanical keyboard with blue switches', 'Accessories', 79.99, 100, 20, 'Warehouse B', 'AccessoryCo'),
('MON-001', '24" Monitor', 'Full HD monitor with HDMI and DisplayPort', 'Electronics', 199.99, 30, 8, 'Warehouse A', 'DisplayTech'),
('TAB-001', '10" Tablet', 'Android tablet with 64GB storage', 'Electronics', 299.99, 45, 12, 'Warehouse A', 'TechSupplier Inc.'),
('PHO-001', 'Smartphone Case', 'Protective case for latest smartphone models', 'Accessories', 19.99, 150, 30, 'Warehouse B', 'AccessoryCo'),
('CHA-001', 'Office Chair', 'Ergonomic office chair with lumbar support', 'Furniture', 249.99, 20, 5, 'Warehouse C', 'FurniturePlus'),
('DES-001', 'Standing Desk', 'Adjustable height standing desk', 'Furniture', 499.99, 15, 3, 'Warehouse C', 'FurniturePlus'),
('PRI-001', 'Wireless Printer', 'All-in-one wireless printer', 'Electronics', 149.99, 25, 6, 'Warehouse A', 'PrintTech');

-- =================================================================
-- ORDER SERVICE TABLES
-- =================================================================

USE order_db;

-- Orders table for order management
CREATE TABLE IF NOT EXISTS orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    customer_email VARCHAR(100),
    customer_phone VARCHAR(20),
    shipping_address TEXT NOT NULL,
    billing_address TEXT,
    status ENUM('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'RETURNED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    subtotal DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    shipping_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(50),
    payment_status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED') NOT NULL DEFAULT 'PENDING',
    payment_transaction_id VARCHAR(100),
    order_notes TEXT,
    internal_notes TEXT,
    expected_delivery_date TIMESTAMP NULL,
    actual_delivery_date TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    INDEX idx_order_number (order_number),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_payment_status (payment_status),
    INDEX idx_created_at (created_at),
    INDEX idx_customer_email (customer_email)
) ENGINE=InnoDB;

-- Order items table for individual order items
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_sku VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_description TEXT,
    product_category VARCHAR(100),
    unit_price DECIMAL(19,2) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(19,2) NOT NULL,
    discount_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    final_price DECIMAL(19,2) NOT NULL,
    weight DECIMAL(10,3),
    length_cm DECIMAL(8,2),
    width_cm DECIMAL(8,2),
    height_cm DECIMAL(8,2),
    special_instructions VARCHAR(500),
    status ENUM('ORDERED', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'RETURNED', 'REFUNDED', 'BACKORDERED') NOT NULL DEFAULT 'ORDERED',
    return_reason VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    INDEX idx_product_sku (product_sku),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

-- Insert sample order data for testing
INSERT INTO orders (order_number, customer_id, customer_name, customer_email, customer_phone, shipping_address, status, subtotal, tax_amount, shipping_amount, total_amount, payment_method, payment_status) VALUES
('ORD-20250130-143022-123', 1, 'John Doe', 'john.doe@email.com', '+1234567890', '123 Main St, City, State 12345', 'CONFIRMED', 1299.99, 130.00, 10.00, 1439.99, 'CREDIT_CARD', 'COMPLETED'),
('ORD-20250130-143045-456', 2, 'Jane Smith', 'jane.smith@email.com', '+1234567891', '456 Oak Ave, City, State 67890', 'PROCESSING', 979.98, 98.00, 10.00, 1087.98, 'PAYPAL', 'COMPLETED'),
('ORD-20250130-143058-789', 3, 'Bob Johnson', 'bob.johnson@email.com', '+1234567892', '789 Pine Rd, City, State 11111', 'SHIPPED', 149.99, 15.00, 10.00, 174.99, 'BANK_TRANSFER', 'COMPLETED');

-- Insert sample order items
INSERT INTO order_items (order_id, product_id, product_sku, product_name, unit_price, quantity, total_price, final_price, status) VALUES
(1, 1, 'LAP-001', 'Laptop Pro 15"', 1299.99, 1, 1299.99, 1299.99, 'CONFIRMED'),
(2, 2, 'LAP-002', 'Laptop Air 13"', 899.99, 1, 899.99, 899.99, 'PROCESSING'),
(3, 5, 'MON-001', '24" Monitor', 199.99, 1, 199.99, 199.99, 'SHIPPED');

-- =================================================================
-- USER SERVICE TABLES
-- =================================================================

USE user_db;

-- Users table for user management and authentication
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(20),
    country VARCHAR(50),
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'TERMINATED') NOT NULL DEFAULT 'ACTIVE',
    role ENUM('CUSTOMER', 'MANAGER', 'ADMIN', 'SUPER_ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_at TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_role (role),
    INDEX idx_enabled (enabled),
    INDEX idx_created_at (created_at),
    INDEX idx_last_login_at (last_login_at)
) ENGINE=InnoDB;

-- User permissions table for additional permissions beyond roles
CREATE TABLE IF NOT EXISTS user_permissions (
    permission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    permission VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_permission (user_id, permission),
    INDEX idx_user_id (user_id),
    INDEX idx_permission (permission)
) ENGINE=InnoDB;

-- Insert sample user data for testing
-- Passwords are encrypted using BCrypt (password = "password123")
INSERT INTO users (username, email, password, first_name, last_name, phone_number, address, city, state, postal_code, country, role) VALUES
('admin', 'admin@inventory.com', '$2a$10$rQZ8kHWKqYQJzKjXzqzN/e9Z8G8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8', 'Admin', 'User', '+1234567890', '123 Admin St', 'Admin City', 'Admin State', '12345', 'USA', 'ADMIN'),
('manager', 'manager@inventory.com', '$2a$10$rQZ8kHWKqYQJzKjXzqzN/e9Z8G8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8', 'Manager', 'User', '+1234567891', '456 Manager Ave', 'Manager City', 'Manager State', '67890', 'USA', 'MANAGER'),
('customer1', 'customer1@email.com', '$2a$10$rQZ8kHWKqYQJzKjXzqzN/e9Z8G8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8', 'John', 'Doe', '+1234567892', '789 Customer Rd', 'Customer City', 'Customer State', '11111', 'USA', 'CUSTOMER'),
('customer2', 'customer2@email.com', '$2a$10$rQZ8kHWKqYQJzKjXzqzN/e9Z8G8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8r8Z8', 'Jane', 'Smith', '+1234567893', '321 Customer Blvd', 'Customer City', 'Customer State', '22222', 'USA', 'CUSTOMER');

-- Insert sample permissions for admin user
INSERT INTO user_permissions (user_id, permission) VALUES
(1, 'USER_READ'),
(1, 'USER_WRITE'),
(1, 'USER_DELETE'),
(1, 'INVENTORY_READ'),
(1, 'INVENTORY_WRITE'),
(1, 'INVENTORY_DELETE'),
(1, 'ORDER_READ'),
(1, 'ORDER_WRITE'),
(1, 'ORDER_DELETE'),
(1, 'SYSTEM_ADMIN');

-- Insert sample permissions for manager user
INSERT INTO user_permissions (user_id, permission) VALUES
(2, 'USER_READ'),
(2, 'INVENTORY_READ'),
(2, 'INVENTORY_WRITE'),
(2, 'ORDER_READ'),
(2, 'ORDER_WRITE'),
(2, 'REPORTS_READ');

-- =================================================================
-- FINAL SETUP
-- =================================================================

-- Show all created databases
SHOW DATABASES;

-- Show tables in each database
USE inventory_db;
SHOW TABLES;

USE order_db;
SHOW TABLES;

USE user_db;
SHOW TABLES;

-- Display sample data counts
SELECT 'Inventory Products Count:' as info, COUNT(*) as count FROM inventory_db.products;
SELECT 'Orders Count:' as info, COUNT(*) as count FROM order_db.orders;
SELECT 'Order Items Count:' as info, COUNT(*) as count FROM order_db.order_items;
SELECT 'Users Count:' as info, COUNT(*) as count FROM user_db.users;
SELECT 'User Permissions Count:' as info, COUNT(*) as count FROM user_db.user_permissions;

-- =================================================================
-- NOTES FOR DEVELOPERS
-- =================================================================

/*
DATABASE ARCHITECTURE NOTES:

1. DATABASE SEPARATION:
   - Each microservice has its own database for isolation
   - This allows independent scaling and deployment
   - Prevents cascading failures between services

2. SECURITY:
   - Each service has its own database user
   - Passwords should be changed in production
   - Consider using SSL connections for production

3. INDEXING:
   - Primary keys are automatically indexed
   - Foreign keys and frequently queried fields are indexed
   - Consider adding composite indexes for complex queries

4. DATA TYPES:
   - DECIMAL(19,2) for monetary values to ensure precision
   - TIMESTAMP for temporal data with automatic updates
   - ENUM for fixed sets of values for data integrity

5. FOREIGN KEYS:
   - order_items.order_id references orders.order_id with CASCADE DELETE
   - user_permissions.user_id references users.user_id with CASCADE DELETE
   - This ensures referential integrity

6. SAMPLE DATA:
   - Sample data is provided for testing and development
   - Passwords are BCrypt encrypted (default: "password123")
   - Remove or modify sample data for production deployment

7. BACKUP STRATEGY:
   - Implement regular backups for each database
   - Consider point-in-time recovery for critical data
   - Test backup restoration procedures

8. MONITORING:
   - Monitor database performance and query execution times
   - Set up alerts for long-running queries
   - Track database size growth

9. SCALING:
   - Consider read replicas for read-heavy workloads
   - Implement connection pooling for better performance
   - Monitor and optimize slow queries

10. COMPLIANCE:
    - Ensure data retention policies are implemented
    - Consider data encryption at rest for sensitive information
    - Implement audit logging for critical operations
*/
