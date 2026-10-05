package com.payflow.authservice.model.enums;

public enum OtpPurpose {

    /**
     * Sent after registration to verify the email address.
     */
    EMAIL_VERIFICATION,

    /**
     * Sent when user requests a password reset. (Phase 3)
     */
    PASSWORD_RESET,

    /**
     * Sent for login on an unrecognized device. (Phase 4)
     */
    LOGIN_2FA
}