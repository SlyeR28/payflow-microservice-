-- ==============================================================================
-- PAYMENT SERVICE SCHEMA INITIALIZATION (V1__init_schema.sql)
-- Database: payment_db
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `payment_transactions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `merchant_id` BIGINT NOT NULL,
    `transaction_reference` VARCHAR(64) NOT NULL,
    `provider` VARCHAR(32) NOT NULL,
    `amount` DECIMAL(15, 2) NOT NULL,
    `currency` VARCHAR(3) NOT NULL,
    `status` VARCHAR(32) NOT NULL,
    `gateway_transaction_id` VARCHAR(128) NULL,
    `customer_email` VARCHAR(255) NULL,
    `description` VARCHAR(255) NULL,
    `idempotency_key` VARCHAR(128) NULL,
    `failure_reason` VARCHAR(500) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pay_reference` (`transaction_reference`),
    UNIQUE KEY `uk_pay_idempotency` (`idempotency_key`),
    INDEX `idx_pay_reference` (`transaction_reference`),
    INDEX `idx_pay_provider` (`provider`),
    INDEX `idx_pay_status` (`status`),
    INDEX `idx_pay_idempotency` (`idempotency_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
