package com.payflow.ledgerservice.model.enums;

/**
 * Operating lifecycle status of an account.
 */
public enum AccountStatus {
    /**
     * Account is active and can process transactions.
     */
    ACTIVE,

    /**
     * Account is temporarily frozen; cannot accept debits or credits.
     */
    FROZEN,

    /**
     * Account is permanently closed.
     */
    CLOSED
}
