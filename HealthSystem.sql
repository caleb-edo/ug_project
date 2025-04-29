create database HealthSystem;
-- Drop existing tables if they exist
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS user;

-- Create user table
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(10) NOT NULL
);

-- Create patient table
CREATE TABLE patient (
    user_id BIGINT PRIMARY KEY,
    full_name VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(10),
    contact_number VARCHAR(15),
    address VARCHAR(200),
    medical_history TEXT,
    allergies TEXT,
    current_medications TEXT,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE doctor (
    user_id BIGINT PRIMARY KEY,
    full_name VARCHAR(100),
    specialization VARCHAR(100),
    license_number VARCHAR(50) UNIQUE NOT NULL,
    contact_number VARCHAR(15),
    address VARCHAR(200),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

ALTER TABLE patient ADD COLUMN doctor_id BIGINT;
ALTER TABLE patient ADD CONSTRAINT fk_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(user_id);

-- Add indexes
CREATE INDEX idx_username ON user(username);
