-- Festival Management System Database Schema
-- MySQL Database Creation Script

-- Create database
CREATE DATABASE IF NOT EXISTS festival_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE festival_db;

-- Create User table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Festival table
CREATE TABLE IF NOT EXISTS festivals (
    festival_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    venue VARCHAR(200) NOT NULL,
    state ENUM('CREATED', 'SUBMISSION', 'ASSIGNMENT', 'REVIEW', 
               'SCHEDULING', 'FINAL_SUBMISSION', 'DECISION', 'ANNOUNCED') 
           DEFAULT 'CREATED' NOT NULL,
    venue_layout JSON,
    budget_info JSON,
    vendor_management JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_state (state),
    INDEX idx_dates (start_date, end_date),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Performance table
CREATE TABLE IF NOT EXISTS performances (
    performance_id INT AUTO_INCREMENT PRIMARY KEY,
    festival_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    genre VARCHAR(50) NOT NULL,
    duration INT NOT NULL COMMENT 'Duration in minutes',
    state ENUM('CREATED', 'SUBMITTED', 'REVIEWED', 
               'REJECTED', 'APPROVED', 'SCHEDULED') 
           DEFAULT 'CREATED' NOT NULL,
    technical_requirements TEXT,
    setlist JSON,
    merchandise_items JSON,
    preferred_times JSON,
    assigned_staff_id INT,
    review_score DECIMAL(3,2),
    review_comments TEXT,
    rejection_reason TEXT,
    scheduled_time DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (festival_id) REFERENCES festivals(festival_id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_staff_id) REFERENCES users(user_id) ON DELETE SET NULL,
    UNIQUE KEY unique_performance_name_per_festival (festival_id, name),
    INDEX idx_festival (festival_id),
    INDEX idx_state (state),
    INDEX idx_genre (genre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create UserRole table (junction table for user roles in festivals)
CREATE TABLE IF NOT EXISTS user_roles (
    user_role_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    festival_id INT NOT NULL,
    role ENUM('ARTIST', 'STAFF', 'ORGANIZER') NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (festival_id) REFERENCES festivals(festival_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_festival_role (user_id, festival_id),
    INDEX idx_user (user_id),
    INDEX idx_festival (festival_id),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create PerformanceArtist table (junction table for artists in performances)
CREATE TABLE IF NOT EXISTS performance_artists (
    perf_artist_id INT AUTO_INCREMENT PRIMARY KEY,
    performance_id INT NOT NULL,
    user_id INT NOT NULL,
    is_main_artist BOOLEAN DEFAULT FALSE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (performance_id) REFERENCES performances(performance_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_performance_artist (performance_id, user_id),
    INDEX idx_performance (performance_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Audit Log table for tracking important actions
CREATE TABLE IF NOT EXISTS audit_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INT NOT NULL,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Rate Limiting table
CREATE TABLE IF NOT EXISTS rate_limits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL COMMENT 'User ID or IP address',
    endpoint VARCHAR(255) NOT NULL,
    request_count INT DEFAULT 1,
    window_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_identifier_endpoint (identifier, endpoint),
    INDEX idx_window (window_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Sessions table for JWT token blacklisting
CREATE TABLE IF NOT EXISTS blacklisted_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    user_id INT,
    expires_at TIMESTAMP NOT NULL,
    blacklisted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default admin user (password: admin123 - should be changed on first login)
-- Password is BCrypt hashed
INSERT INTO users (username, password, full_name) VALUES 
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36LF8zdA9M7xg9Yd4KvunKm', 'System Administrator');

-- Create views for common queries
CREATE OR REPLACE VIEW festival_summary AS
SELECT 
    f.festival_id,
    f.name,
    f.state,
    f.start_date,
    f.end_date,
    COUNT(DISTINCT p.performance_id) as total_performances,
    COUNT(DISTINCT CASE WHEN p.state = 'SCHEDULED' THEN p.performance_id END) as scheduled_performances,
    COUNT(DISTINCT ur.user_id) as total_staff
FROM festivals f
LEFT JOIN performances p ON f.festival_id = p.festival_id
LEFT JOIN user_roles ur ON f.festival_id = ur.festival_id AND ur.role = 'STAFF'
GROUP BY f.festival_id;

CREATE OR REPLACE VIEW performance_details AS
SELECT 
    p.*,
    u.full_name as assigned_staff_name,
    GROUP_CONCAT(DISTINCT pa_user.full_name) as artists
FROM performances p
LEFT JOIN users u ON p.assigned_staff_id = u.user_id
LEFT JOIN performance_artists pa ON p.performance_id = pa.performance_id
LEFT JOIN users pa_user ON pa.user_id = pa_user.user_id
GROUP BY p.performance_id;

-- Stored procedure for state transitions
DELIMITER $$

CREATE PROCEDURE advance_festival_state(
    IN p_festival_id INT,
    IN p_user_id INT
)
BEGIN
    DECLARE current_state VARCHAR(20);
    DECLARE new_state VARCHAR(20);
    DECLARE is_organizer BOOLEAN DEFAULT FALSE;
    
    -- Check if user is organizer
    SELECT COUNT(*) > 0 INTO is_organizer
    FROM user_roles
    WHERE user_id = p_user_id 
      AND festival_id = p_festival_id 
      AND role = 'ORGANIZER';
    
    IF NOT is_organizer THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'User is not an organizer of this festival';
    END IF;
    
    -- Get current state
    SELECT state INTO current_state
    FROM festivals
    WHERE festival_id = p_festival_id;
    
    -- Determine next state
    CASE current_state
        WHEN 'CREATED' THEN SET new_state = 'SUBMISSION';
        WHEN 'SUBMISSION' THEN SET new_state = 'ASSIGNMENT';
        WHEN 'ASSIGNMENT' THEN SET new_state = 'REVIEW';
        WHEN 'REVIEW' THEN SET new_state = 'SCHEDULING';
        WHEN 'SCHEDULING' THEN SET new_state = 'FINAL_SUBMISSION';
        WHEN 'FINAL_SUBMISSION' THEN SET new_state = 'DECISION';
        WHEN 'DECISION' THEN SET new_state = 'ANNOUNCED';
        ELSE 
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Invalid state transition';
    END CASE;
    
    -- Update state
    UPDATE festivals 
    SET state = new_state 
    WHERE festival_id = p_festival_id;
    
    -- Log the action
    INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_value, new_value)
    VALUES (p_user_id, 'ADVANCE_STATE', 'FESTIVAL', p_festival_id, current_state, new_state);
    
END$$

DELIMITER ;

-- Indexes for performance optimization
CREATE INDEX idx_performance_search ON performances(name, genre);
CREATE INDEX idx_festival_search ON festivals(name, venue);

-- Grant permissions (adjust as needed)
-- GRANT ALL PRIVILEGES ON festival_db.* TO 'festival_user'@'localhost';
-- FLUSH PRIVILEGES;