-- ============================================================
--  PropertyIQ - Database Schema
--  Run this script once to set up your MySQL database
-- ============================================================

CREATE DATABASE IF NOT EXISTS propertyiq_db;
USE propertyiq_db;

-- -----------------------------------------------------------
-- USERS TABLE
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,   -- BCrypt hash
    role        ENUM('ADMIN', 'INVESTOR') NOT NULL DEFAULT 'INVESTOR',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------
-- PROPERTIES TABLE
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS properties (
    property_id     INT AUTO_INCREMENT PRIMARY KEY,
    property_name   VARCHAR(100) NOT NULL,
    location        VARCHAR(100) NOT NULL,
    purchase_price  DECIMAL(15, 2) NOT NULL,
    rental_income   DECIMAL(10, 2) NOT NULL,   -- monthly
    expenses        DECIMAL(10, 2) NOT NULL,   -- monthly
    location_rating INT NOT NULL CHECK (location_rating BETWEEN 1 AND 10),
    risk_level      ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL DEFAULT 'MEDIUM',
    created_by      INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- -----------------------------------------------------------
-- ANALYSIS TABLE
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS analysis (
    analysis_id         INT AUTO_INCREMENT PRIMARY KEY,
    property_id         INT NOT NULL,
    roi                 DECIMAL(10, 4) NOT NULL,
    monthly_profit      DECIMAL(10, 2) NOT NULL,
    annual_yield        DECIMAL(10, 4) NOT NULL,
    investment_score    DECIMAL(5, 2)  NOT NULL,
    recommendation      ENUM('HIGH_PROFIT', 'MODERATE', 'RISKY') NOT NULL,
    calculated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(property_id) ON DELETE CASCADE
);

-- -----------------------------------------------------------
-- SEED DATA - Default admin account
-- Password: admin123  (BCrypt hashed)
-- -----------------------------------------------------------
INSERT INTO users (username, password, role) VALUES
('admin', '$2a$10$Ohlb9ZEMja0Uu.otPACBQ.ShMIghaSdqXJ83CQT3SaC0Z4TyytLdC', 'ADMIN'),
('investor1', '$2a$10$Ohlb9ZEMja0Uu.otPACBQ.ShMIghaSdqXJ83CQT3SaC0Z4TyytLdC', 'INVESTOR');

-- Sample properties for testing
INSERT INTO properties (property_name, location, purchase_price, rental_income, expenses, location_rating, risk_level, created_by) VALUES
('Maple Heights Condo',  'Kuala Lumpur', 450000.00, 2800.00, 600.00,  8, 'LOW',    1),
('Sunrise Terrace',      'Penang',       320000.00, 1900.00, 450.00,  7, 'MEDIUM', 1),
('Harbor View Flat',     'Johor Bahru',  280000.00, 1600.00, 380.00,  6, 'MEDIUM', 1),
('Green Valley Bungalow','Selangor',     750000.00, 3800.00, 900.00,  9, 'LOW',    1),
('City Square Studio',   'Kuala Lumpur', 210000.00, 1400.00, 320.00,  8, 'HIGH',   1);