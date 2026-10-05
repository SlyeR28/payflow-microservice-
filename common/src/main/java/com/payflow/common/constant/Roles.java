package com.payflow.common.constant;

/**
 * Roles used across every PayFlow service.
 *
 * Stored in JWTs as strings (enum name), stored in DB as strings.
 * Spring Security authorities are prefixed with "ROLE_" at the filter.
 *
 * For Phase 1, one role per user.
 */
public enum Roles {

    /**
     * End customer who pays merchants.
     * Can: initiate payments, view own transaction history.
     */
    CUSTOMER,

    /**
     * Business that accepts payments.
     * Can: manage gateway keys, view incoming payments, issue refunds.
     */
    MERCHANT,

    /**
     * Platform staff handling customer/merchant issues.
     * Can: view all transactions (read-only), issue refunds, view merchant details.
     */
    SUPPORT,

    /**
     * Finance team. Handles reconciliation, settlement review, disputes.
     * Can: view ledger, run reconciliation, approve/reject settlements.
     */
    FINANCE,

    /**
     * Platform super admin.
     * Can: everything, including merchant approval, suspension, config changes.
     */
    ADMIN;

    /**
     * Spring Security authority form.
     * hasRole("ADMIN") → looks for "ROLE_ADMIN" in the authorities list.
     */
    public String authority() {
        return "ROLE_" + this.name();
    }
}