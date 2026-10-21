-- ==============================================================================
-- PayFlow MySQL Database Auto-Initialization
-- Runs once when MySQL container is initialized for the first time
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS `secuirty` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `merchant` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `payment_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `payflow_ledger` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant full privileges to root user across Docker network
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';
FLUSH PRIVILEGES;
