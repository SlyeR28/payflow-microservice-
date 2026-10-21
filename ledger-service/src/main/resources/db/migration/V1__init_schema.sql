-- ==============================================================================
-- LEDGER SERVICE SCHEMA INITIALIZATION (V1__init_schema.sql)
-- Database: payflow_ledger
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `ledger_accounts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account_number` VARCHAR(64) NOT NULL,
    `name` VARCHAR(128) NOT NULL,
    `merchant_id` BIGINT NULL,
    `account_type` VARCHAR(32) NOT NULL,
    `currency` VARCHAR(3) NOT NULL DEFAULT 'INR',
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ledger_acc_number` (`account_number`),
    INDEX `idx_ledger_merchant_id` (`merchant_id`),
    INDEX `idx_ledger_acc_type` (`account_type`),
    INDEX `idx_ledger_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ledger_account_balances` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account_id` BIGINT NOT NULL,
    `current_balance` DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    `available_balance` DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    `pending_balance` DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    `version` BIGINT NOT NULL DEFAULT 0,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_balance_account` (`account_id`),
    CONSTRAINT `fk_balance_account_id` FOREIGN KEY (`account_id`) REFERENCES `ledger_accounts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ledger_journal_entries` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `entry_reference` VARCHAR(64) NOT NULL,
    `source_system` VARCHAR(64) NOT NULL,
    `entry_type` VARCHAR(32) NOT NULL,
    `description` VARCHAR(255) NULL,
    `idempotency_key` VARCHAR(128) NULL,
    `posted_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_journal_entry_ref` (`entry_reference`),
    UNIQUE KEY `uk_journal_idempotency` (`idempotency_key`),
    INDEX `idx_journal_entry_type` (`entry_type`),
    INDEX `idx_journal_posted_at` (`posted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ledger_postings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `journal_entry_id` BIGINT NOT NULL,
    `account_id` BIGINT NOT NULL,
    `direction` VARCHAR(16) NOT NULL,
    `amount` DECIMAL(19, 4) NOT NULL,
    `currency` VARCHAR(3) NOT NULL DEFAULT 'INR',
    `created_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_posting_account` (`account_id`),
    INDEX `idx_posting_journal` (`journal_entry_id`),
    INDEX `idx_posting_created_at` (`created_at`),
    CONSTRAINT `fk_posting_journal_id` FOREIGN KEY (`journal_entry_id`) REFERENCES `ledger_journal_entries` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_posting_account_id` FOREIGN KEY (`account_id`) REFERENCES `ledger_accounts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
