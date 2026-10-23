-- ========================================================
-- Campus Event Recommendation System - MySQL Database Schema
-- Run this script in MySQL Workbench, phpMyAdmin, or mysql CLI
-- ========================================================

-- 1. Create Database
CREATE DATABASE IF NOT EXISTS campus_events_db;
USE campus_events_db;

-- 2. Users Table for Role-Based Authentication
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,         -- Stores 64-char SHA-256 hexadecimal hash
    role VARCHAR(20) NOT NULL,              -- 'STUDENT' or 'COORDINATOR'
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

-- 3. Events Table
CREATE TABLE IF NOT EXISTS events (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    category VARCHAR(50) NOT NULL,          -- e.g., 'Coding', 'AI', 'Robotics', 'Sports', 'Arts'
    event_date VARCHAR(20) NOT NULL,        -- Format: YYYY-MM-DD
    time_slot VARCHAR(20) NOT NULL,         -- 'Morning', 'Afternoon', 'Evening'
    fee DOUBLE NOT NULL DEFAULT 0.0,
    venue VARCHAR(120) NOT NULL,
    required_skills VARCHAR(255),           -- Comma-separated: 'Java, Python'
    eligible_dept VARCHAR(100) DEFAULT 'All',
    eligible_year INT DEFAULT 0             -- 0 = All Years, or 1, 2, 3, 4
);

-- 4. Students Table
CREATE TABLE IF NOT EXISTS students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    department VARCHAR(50) NOT NULL,
    academic_year INT NOT NULL,
    budget DOUBLE NOT NULL DEFAULT 0.0,
    interests VARCHAR(255),
    skills VARCHAR(255),
    availability VARCHAR(50)
);

-- 5. Registrations Table
CREATE TABLE IF NOT EXISTS registrations (
    reg_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT DEFAULT 0,
    student_name VARCHAR(100) NOT NULL,
    student_email VARCHAR(100) NOT NULL,
    event_id INT NOT NULL,
    event_title VARCHAR(120) NOT NULL,
    registered_at VARCHAR(50) NOT NULL,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
);

-- 6. Starter User Accounts (Students register dynamically via registration form)
-- Coordinator password 'admin123' hashed with SHA-256
INSERT IGNORE INTO users (username, password, role, full_name, email) VALUES
('coordinator', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'COORDINATOR', 'Dr. Sarah Jenkins', 'sarah.jenkins@campus.edu');
