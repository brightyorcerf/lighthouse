-- ============================================================
--  Lighthouse - Database Schema
--  Run this script once to set up your MySQL database
-- ============================================================

CREATE DATABASE IF NOT EXISTS lighthouse_db;
USE lighthouse_db;

-- -----------------------------------------------------------
-- SYSTEM SETTINGS TABLE (For Rules)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS system_settings (
    setting_key   VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(100) NOT NULL
);

-- -----------------------------------------------------------
-- USERS TABLE
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        ENUM('ADMIN', 'INVESTOR') NOT NULL DEFAULT 'INVESTOR',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------
-- LOCATIONS TABLE
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS locations (
    location_id   INT AUTO_INCREMENT PRIMARY KEY,
    location_name VARCHAR(100) NOT NULL UNIQUE,
    rating        INT NOT NULL CHECK (rating BETWEEN 1 AND 10),
    risk          INT NOT NULL CHECK (risk BETWEEN 1 AND 10)
);

-- -----------------------------------------------------------
-- PROPERTIES TABLE
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS properties (
    property_id   INT AUTO_INCREMENT PRIMARY KEY,
    property_name VARCHAR(100) NOT NULL,
    location_id   INT NOT NULL,
    price         DECIMAL(15, 2) NOT NULL,
    rent          DECIMAL(10, 2) NOT NULL,
    cost          DECIMAL(10, 2) NOT NULL,
    created_by    INT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (location_id) REFERENCES locations(location_id) ON DELETE RESTRICT,
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

-- Insert default locations
INSERT INTO locations (location_name, rating, risk) VALUES
('Mumbai', 9, 4),
('Hyderabad', 8, 3),
('Delhi', 7, 6),
('Bangalore', 9, 3),
('Pune', 8, 4);

-- Insert default system rules
INSERT INTO system_settings (setting_key, setting_value) VALUES
('rule_high_price_threshold', '500000'),
('rule_low_rent_threshold', '2000'),
('rule_high_price_low_rent_risk_penalty', '3'),
('rule_low_demand_rating_threshold', '5'),
('rule_low_demand_risk_penalty', '2');

-- Sample properties for testing
INSERT INTO properties (property_name, location_id, price, rent, cost, created_by) VALUES
('Maple Heights Condo',  1, 450000.00, 2800.00, 600.00,  1),
('Sunrise Terrace',      2, 320000.00, 1900.00, 450.00,  1),
('Harbor View Flat',     3, 280000.00, 1600.00, 380.00,  1),
('Green Valley Bungalow',4, 750000.00, 3800.00, 900.00,  1),
('City Square Studio',   5, 210000.00, 1400.00, 320.00,  1);