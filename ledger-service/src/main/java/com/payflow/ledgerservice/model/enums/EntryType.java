package com.payflow.ledgerservice.model.enums;

/**
 * Business event type represented by a Journal Entry.
 */
public enum EntryType {
    /**
     * Customer payment captured and settled to merchant & platform revenue.
     */
    PAYMENT_SETTLEMENT,

    /**
     * Merchant withdrawal/payout to their external bank account.
     */
    MERCHANT_PAYOUT,

    /**
     * Explicit platform processing fee deduction.
     */
    PLATFORM_FEE,

    /**
     * Customer refund issued, reversing previous settlement.
     */
    REFUND_REVERSAL,

    /**
     * Chargeback or dispute hold reserved from merchant balance.
     */
    DISPUTE_HOLD
}
