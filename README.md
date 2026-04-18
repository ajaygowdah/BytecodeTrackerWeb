# Custom JVM Bytecode Analyzer

A web application to upload and analyze Java bytecode files for potential security vulnerabilities.

## Features
- User registration and login
- Upload .class files for analysis
- Detect dangerous methods (exec, delete, system, etc.)
- Database storage of analysis results
- Real-time bytecode scanning

## Tech Stack
- **Backend:** Java (HTTP Server)
- **Frontend:** HTML, CSS
- **Database:** MySQL
- **Bytecode Analysis:** Java Reflection API

## Setup Instructions

### Prerequisites
- Java 8 or higher
- MySQL Server
- MySQL JDBC Driver (mysql-connector-j-9.6.0.jar)

### Database Setup
```sql
CREATE DATABASE bytecode_db;
USE bytecode_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE scans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    file_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE classes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    scan_id INT NOT NULL,
    class_name VARCHAR(255),
    FOREIGN KEY (scan_id) REFERENCES scans(id)
);

CREATE TABLE methods (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_id INT NOT NULL,
    method_name VARCHAR(255),
    status VARCHAR(50),
    FOREIGN KEY (class_id) REFERENCES classes(id)
);

CREATE TABLE violations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    method_id INT NOT NULL,
    description VARCHAR(500),
    FOREIGN KEY (method_id) REFERENCES methods(id)
);