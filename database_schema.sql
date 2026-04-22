-- Donation Management System Database Schema
-- MySQL Database
-- Compatible with template requirements

-- Create the database
CREATE DATABASE IF NOT EXISTS donation_system;
USE donation_system;

-- Users table (stores all user types)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    user_type ENUM('DONOR', 'RECEIVER', 'ADMIN', 'VOLUNTEER') NOT NULL,
    location VARCHAR(100),
    is_verified BOOLEAN DEFAULT FALSE,
    role VARCHAR(50),
    working_hours INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_user_type (user_type)
);

-- Charity Cases table
CREATE TABLE IF NOT EXISTS charity_cases (
    case_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type VARCHAR(100) NOT NULL,
    description TEXT,
    status ENUM('Pending', 'Accepted - High Priority', 'Accepted - Low Priority', 'Declined', 'Completed') DEFAULT 'Pending',
    priority_level ENUM('None', 'High', 'Low') DEFAULT 'None',
    goal_amount DECIMAL(15, 2) DEFAULT 0.00,
    current_amount DECIMAL(15, 2) DEFAULT 0.00,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_user (user_id)
);

-- Donations table
CREATE TABLE IF NOT EXISTS donations (
    donation_id INT AUTO_INCREMENT PRIMARY KEY,
    donor_id INT NOT NULL,
    case_id INT,
    donation_type ENUM('Financial', 'Medical', 'Other') NOT NULL,
    amount DECIMAL(15, 2) DEFAULT 0.00,
    quantity INT DEFAULT 0,
    medical_equipment VARCHAR(200),
    category VARCHAR(100),
    date DATE NOT NULL,
    status ENUM('Pending', 'Completed', 'Cancelled') DEFAULT 'Completed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES charity_cases(case_id) ON DELETE SET NULL,
    INDEX idx_donor (donor_id),
    INDEX idx_case (case_id),
    INDEX idx_type (donation_type)
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_read (is_read)
);

-- Verification Codes table (for email verification)
CREATE TABLE IF NOT EXISTS verification_codes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    code VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_code (code)
);

-- Insert default admin user (password: admin123)
-- BCrypt hash for 'admin123'
INSERT INTO users (name, email, phone, password, user_type, location, is_verified) 
VALUES ('Admin', 'admin@donation.com', '0000000000', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.AoN5VKMYqVKZz8.Xqu', 'ADMIN', 'Main Office', TRUE)
ON DUPLICATE KEY UPDATE name = name;

-- Insert sample receiver for testing
INSERT INTO users (name, email, phone, password, user_type, is_verified) 
VALUES ('Test Receiver', 'receiver@test.com', '1111111111', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.AoN5VKMYqVKZz8.Xqu', 'RECEIVER', FALSE)
ON DUPLICATE KEY UPDATE name = name;

-- Insert sample donor for testing
INSERT INTO users (name, email, phone, password, user_type, is_verified) 
VALUES ('Test Donor', 'donor@test.com', '2222222222', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.AoN5VKMYqVKZz8.Xqu', 'DONOR', TRUE)
ON DUPLICATE KEY UPDATE name = name;

-- Insert sample volunteer for testing
INSERT INTO users (name, email, phone, password, user_type, location, role, working_hours, is_verified) 
VALUES ('Test Volunteer', 'volunteer@test.com', '3333333333', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.AoN5VKMYqVKZz8.Xqu', 'VOLUNTEER', 'Field Office', 'Case Coordinator', 0, FALSE)
ON DUPLICATE KEY UPDATE name = name;

-- Sample charity cases for testing
INSERT INTO charity_cases (user_id, type, description, status, priority_level, goal_amount, category) 
SELECT id, 'Medical Emergency', 'Help cover medical expenses for emergency treatment', 'Accepted - High Priority', 'High', 5000.00, 'Medical'
FROM users WHERE email = 'receiver@test.com' LIMIT 1;

INSERT INTO charity_cases (user_id, type, description, status, priority_level, goal_amount, category) 
SELECT id, 'Education Support', 'Support education for underprivileged children', 'Accepted - Low Priority', 'Low', 3000.00, 'Education'
FROM users WHERE email = 'receiver@test.com' LIMIT 1;

INSERT INTO charity_cases (user_id, type, description, status, priority_level, goal_amount, category) 
SELECT id, 'Emergency Housing', 'Temporary housing assistance needed', 'Pending', 'None', 2000.00, 'Housing'
FROM users WHERE email = 'receiver@test.com' LIMIT 1;

-- View for donation statistics
CREATE OR REPLACE VIEW donation_statistics AS
SELECT 
    COUNT(DISTINCT d.donor_id) as total_donors,
    COUNT(d.donation_id) as total_donations,
    COALESCE(SUM(d.amount), 0) as total_amount,
    COALESCE(AVG(d.amount), 0) as average_donation
FROM donations d
WHERE d.status = 'Completed';

-- View for case progress
CREATE OR REPLACE VIEW case_progress AS
SELECT 
    cc.case_id,
    cc.type,
    cc.goal_amount,
    cc.current_amount,
    ROUND((cc.current_amount / NULLIF(cc.goal_amount, 0)) * 100, 2) as progress_percentage,
    cc.status,
    u.name as receiver_name
FROM charity_cases cc
JOIN users u ON cc.user_id = u.id;

-- Stored procedure to update case amount after donation
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS update_case_amount(IN p_case_id INT, IN p_amount DECIMAL(15,2))
BEGIN
    UPDATE charity_cases 
    SET current_amount = current_amount + p_amount,
        status = CASE 
            WHEN (current_amount + p_amount) >= goal_amount THEN 'Completed'
            ELSE status
        END
    WHERE case_id = p_case_id;
END //
DELIMITER ;

-- Show tables for verification
SHOW TABLES;
