-- FutureMe database schema (MySQL 8.x)
-- Run this once in MySQL Workbench or the mysql command-line client.
 
CREATE DATABASE IF NOT EXISTS futureme_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
 
USE futureme_db;
 
CREATE TABLE IF NOT EXISTS letters (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(150) NOT NULL,
    content     TEXT         NOT NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    unlock_at   DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_letters_unlock_at (unlock_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
