package com.payflow.authservice.model.enums;

public enum UserStatus {

    /**
     * Registered but email not yet verified.
     * Cannot log in until emailVerified = true.
     */
    PENDING_VERIFICATION,

    /**
     * Normal state. User can log in.
     */
    ACTIVE,

    /**
     * Admin action. Cannot log in until admin reverses.
     * Not automatic — different from lockout (which expires via lockedUntil).
     */
    SUSPENDED,

    /**
     * User deleted their own account.
     */
    DEACTIVATED
}