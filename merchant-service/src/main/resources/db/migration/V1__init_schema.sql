-- ==============================================================================
-- MERCHANT SERVICE SCHEMA INITIALIZATION (V1__init_schema.sql)
-- Database: merchant
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `merchants` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `business_name` VARCHAR(200) NOT NULL,
    `legal_name` VARCHAR(255) NOT NULL,
    `business_email` VARCHAR(255) NOT NULL,
    `business_phone` VARCHAR(20) NULL,
    `website` VARCHAR(255) NULL,
    `business_category` VARCHAR(100) NULL,
    `pan_number_masked` VARCHAR(20) NULL,
    `pan_number_encrypted` TEXT NULL,
    `pan_number_hash` VARCHAR(64) NULL,
    `is_pan_verified` BIT(1) NOT NULL DEFAULT b'0',
    `pan_verified_at` DATETIME(6) NULL,
    `is_bank_verified` BIT(1) NOT NULL DEFAULT b'0',
    `bank_verified_at` DATETIME(6) NULL,
    `gstin` VARCHAR(30) NULL,
    `status` VARCHAR(50) NOT NULL,
    `business_type` VARCHAR(50) NOT NULL,
    `rejection_reason` VARCHAR(500) NULL,
    `approved_at` DATETIME(6) NULL,
    `approved_by` BIGINT NULL,
    `escrow_account_number` VARCHAR(64) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_merchants_user_id` (`user_id`),
    INDEX `idx_merchants_user_id` (`user_id`),
    INDEX `idx_merchants_business_email` (`business_email`),
    INDEX `idx_merchants_pan_hash` (`pan_number_hash`),
    INDEX `idx_merchants_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `merchant_addresses` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `merchant_id` BIGINT NOT NULL,
    `address_type` VARCHAR(50) NOT NULL,
    `label` VARCHAR(100) NULL,
    `address_line1` VARCHAR(200) NOT NULL,
    `address_line2` VARCHAR(200) NULL,
    `city` VARCHAR(100) NOT NULL,
    `state` VARCHAR(100) NULL,
    `postal_code` VARCHAR(20) NULL,
    `country` VARCHAR(100) NOT NULL,
    `is_primary` BIT(1) NOT NULL DEFAULT b'0',
    `address_hash` VARCHAR(64) NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_addresses_merchant_id` (`merchant_id`),
    INDEX `idx_addresses_type` (`merchant_id`, `address_type`),
    INDEX `idx_addresses_merchant_hash` (`merchant_id`, `address_hash`),
    INDEX `idx_addresses_global_hash` (`address_hash`),
    CONSTRAINT `fk_addresses_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `merchant_kyc` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `merchant_id` BIGINT NOT NULL,
    `document_type` VARCHAR(50) NOT NULL,
    `document_number_masked` VARCHAR(50) NULL,
    `s3_object_key` VARCHAR(500) NOT NULL,
    `content_type` VARCHAR(100) NULL,
    `file_size` BIGINT NULL,
    `verification_reference_id` VARCHAR(100) NULL,
    `status` VARCHAR(50) NOT NULL,
    `rejection_reason` VARCHAR(500) NULL,
    `verified_at` DATETIME(6) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_kyc_merchant_id` (`merchant_id`),
    INDEX `idx_kyc_status` (`status`),
    INDEX `idx_kyc_doc_type` (`merchant_id`, `document_type`),
    CONSTRAINT `fk_kyc_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `merchant_bank_accounts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `merchant_id` BIGINT NOT NULL,
    `account_holder_name` VARCHAR(200) NOT NULL,
    `account_number_encrypted` TEXT NOT NULL,
    `account_number_hash` VARCHAR(64) NOT NULL,
    `account_number_last4` VARCHAR(4) NOT NULL,
    `account_number_key_version` INT NULL DEFAULT 1,
    `ifsc_code` VARCHAR(20) NOT NULL,
    `bank_name` VARCHAR(100) NOT NULL,
    `beneficiary_name` VARCHAR(150) NULL,
    `name_match_score` DOUBLE NULL,
    `is_primary` BIT(1) NOT NULL DEFAULT b'0',
    `is_verified` BIT(1) NOT NULL DEFAULT b'0',
    `verification_reference_id` VARCHAR(100) NULL,
    `verified_at` DATETIME(6) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_bank_merchant_id` (`merchant_id`),
    INDEX `idx_bank_account_hash` (`account_number_hash`),
    INDEX `idx_bank_primary` (`merchant_id`, `is_primary`),
    CONSTRAINT `fk_bank_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `merchant_gateway_configs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `merchant_id` BIGINT NOT NULL,
    `gateway_type` VARCHAR(30) NOT NULL,
    `api_key_encrypted` TEXT NOT NULL,
    `api_secret_encrypted` TEXT NOT NULL,
    `webhook_secret_encrypted` TEXT NULL,
    `is_active` BIT(1) NOT NULL,
    `is_test_mode` BIT(1) NOT NULL,
    `verified_at` DATETIME(6) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_merchant_gateway` (`merchant_id`, `gateway_type`),
    INDEX `idx_gateway_merchant_active` (`merchant_id`, `is_active`),
    CONSTRAINT `fk_gateway_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
