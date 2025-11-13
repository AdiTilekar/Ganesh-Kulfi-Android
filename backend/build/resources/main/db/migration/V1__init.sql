-- Ganesh Kulfi Backend - Initial MySQL Schema
-- Version: 1 (MySQL)
-- Description: Creates user table with roles and pricing tiers

-- Create database if not exists (comment out if running from specific database)
-- CREATE DATABASE IF NOT EXISTS ganeshkulfi_db;
-- USE ganeshkulfi_db;

-- Create app_user table
CREATE TABLE IF NOT EXISTS app_user (
    id CHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('ADMIN', 'RETAILER', 'CUSTOMER', 'GUEST') NOT NULL DEFAULT 'CUSTOMER',
    retailer_id VARCHAR(50) UNIQUE,
    shop_name VARCHAR(255),
    tier ENUM('VIP', 'PREMIUM', 'REGULAR', 'RETAIL'),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_retailer_id (retailer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default admin user
-- Password: Admin1234 (bcrypt hash)
INSERT INTO app_user (id, email, password_hash, name, role, created_at, updated_at)
VALUES (
    UUID(),
    'admin@ganeshkulfi.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVKK6RZZ3i',
    'Admin',
    'ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON DUPLICATE KEY UPDATE email = email;

-- Insert default retailer user
-- Password: Retailer1234 (bcrypt hash)
INSERT INTO app_user (id, email, password_hash, name, phone, role, retailer_id, shop_name, tier, created_at, updated_at)
VALUES (
    UUID(),
    'retailer@test.com',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Test Retailer',
    '9876543210',
    'RETAILER',
    'RET001',
    'Sweet Dreams Kulfi Shop',
    'VIP',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON DUPLICATE KEY UPDATE email = email;

-- Verify data
SELECT 'Users created successfully!' as status;
SELECT id, email, name, role, tier, retailer_id FROM app_user;
