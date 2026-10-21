-- ==============================================================================
-- AUTH SERVICE SCHEMA INITIALIZATION (V1__init_schema.sql)
-- Database: secuirty
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_name` VARCHAR(50) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `first_name` VARCHAR(50) NULL,
    `last_name` VARCHAR(50) NULL,
    `phone_number` VARCHAR(20) NULL,
    `avatar_url` VARCHAR(500) NULL,
    `user_status` VARCHAR(50) NOT NULL,
    `email_verified` BIT(1) NOT NULL DEFAULT b'0',
    `failed_attempts` INT NOT NULL DEFAULT 0,
    `last_login_at` DATETIME(6) NULL,
    `locked_until` DATETIME(6) NULL,
    `username_changed_at` DATETIME(6) NULL,
    `last_username_reminder_at` DATETIME(6) NULL,
    `username_reminder_count` INT NULL DEFAULT 0,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_user_name` (`user_name`),
    UNIQUE KEY `uk_users_email` (`email`),
    INDEX `idx_users_username` (`user_name`),
    INDEX `idx_users_email` (`email`),
    INDEX `idx_users_username_reminder` (`username_changed_at`, `user_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_roles` (
    `user_id` BIGINT NOT NULL,
    `role` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`user_id`, `role`),
    CONSTRAINT `fk_user_roles_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_credentials` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `password_changed_at` DATETIME(6) NOT NULL,
    `reset_token` VARCHAR(64) NULL,
    `reset_token_expires_at` DATETIME(6) NULL,
    `reset_token_consumed` BIT(1) NULL DEFAULT b'1',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_credentials_user_id` (`user_id`),
    INDEX `idx_users_user_id` (`user_id`),
    CONSTRAINT `fk_user_credentials_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `refresh_tokens` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `token_hash` VARCHAR(64) NOT NULL,
    `user_id` BIGINT NOT NULL,
    `family_id` VARCHAR(36) NOT NULL,
    `device_info` VARCHAR(255) NULL,
    `ip_address` VARCHAR(45) NULL,
    `expires_at` DATETIME(6) NOT NULL,
    `revoked` BIT(1) NOT NULL DEFAULT b'0',
    `revoked_reason` VARCHAR(50) NULL,
    `created_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refresh_token_hash` (`token_hash`),
    INDEX `idx_refresh_token_hash` (`token_hash`),
    INDEX `idx_refresh_token_user_id` (`user_id`),
    INDEX `idx_refresh_token_family_id` (`family_id`),
    INDEX `idx_refresh_token_expires_at` (`expires_at`),
    CONSTRAINT `fk_refresh_tokens_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `otp_codes` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(255) NOT NULL,
    `code_hash` VARCHAR(64) NOT NULL,
    `purpose` VARCHAR(30) NOT NULL,
    `expires_at` DATETIME(6) NOT NULL,
    `consumed` BIT(1) NOT NULL DEFAULT b'0',
    `attempts` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_otp_email_consumed` (`email`, `consumed`),
    INDEX `idx_otp_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
