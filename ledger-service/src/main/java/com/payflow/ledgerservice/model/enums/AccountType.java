package com.payflow.ledgerservice.model.enums;

/**
 * Standard financial account classification for double-entry bookkeeping.
 * Controls normal balance behavior (Debit vs Credit).
 */
public enum AccountType {
    /**
     * Economic resources owned by PayFlow or held with gateways/banks.
     * Normal balance: DEBIT (+)
     */
    ASSET,

    /**
     * Obligations owed by PayFlow to external entities (e.g., merchants).
     * Normal balance: CREDIT (-)
     */
    LIABILITY,

    /**
     * Gross income earned by PayFlow operations (platform commission, markups).
     * Normal balance: CREDIT (-)
     */
    REVENUE,

    /**
     * Operational costs incurred by PayFlow (gateway interchange fees, SMS costs).
     * Normal balance: DEBIT (+)
     */
    EXPENSE,

    /**
     * Platform retained earnings and capital reserves.
     * Normal balance: CREDIT (-)
     */
    EQUITY
}
