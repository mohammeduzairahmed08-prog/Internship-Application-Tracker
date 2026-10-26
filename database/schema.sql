-- ========================================================================
-- Internship Application Tracker - Database Schema Script
-- Target Database: MySQL 8.x
-- ========================================================================

-- Create database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS internship_tracker
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE internship_tracker;

-- ------------------------------------------------------------------------
-- Table: users
-- Description: Stores authenticated users with hashed passwords
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- Table: applications
-- Description: Stores internship and job application records per user
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(150) NOT NULL,
    job_role VARCHAR(150) NOT NULL,
    job_description TEXT,
    application_url VARCHAR(500),
    company_website VARCHAR(255),
    location VARCHAR(150),
    work_mode VARCHAR(50) NOT NULL DEFAULT 'REMOTE',
    employment_type VARCHAR(50) DEFAULT 'INTERNSHIP',
    application_date DATE NOT NULL,
    deadline DATE,
    stipend DECIMAL(12, 2) DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'APPLIED',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_applications_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_applications_user_id (user_id),
    INDEX idx_applications_status (status),
    INDEX idx_applications_deadline (deadline),
    INDEX idx_applications_company (company_name),
    INDEX idx_applications_role (job_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- Table: interviews
-- Description: Tracks multiple interview rounds scheduled for an application
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS interviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    interview_round VARCHAR(100) NOT NULL,
    interview_date DATETIME NOT NULL,
    interview_mode VARCHAR(50) NOT NULL DEFAULT 'ONLINE',
    meeting_link VARCHAR(500),
    interviewer_name VARCHAR(150),
    preparation_notes TEXT,
    outcome VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interviews_application FOREIGN KEY (application_id)
        REFERENCES applications (id) ON DELETE CASCADE,
    INDEX idx_interviews_application_id (application_id),
    INDEX idx_interviews_date (interview_date),
    INDEX idx_interviews_outcome (outcome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
