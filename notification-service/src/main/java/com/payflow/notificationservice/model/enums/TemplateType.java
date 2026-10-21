package com.payflow.notificationservice.model.enums;

public enum TemplateType {
    OTP_ALERT,              // Authentication and high-value payment verification
    PAYMENT_SUCCESS,        // Customer order checkout receipt
    PAYMENT_FAILED,         // Card decline / insufficient funds alert
    REFUND_PROCESSED,       // Customer refund confirmation
    MERCHANT_PAYOUT,        // Bank payout transfer notification
    KYC_STATUS_UPDATE,
    MERCHANT_APPROVED,      // Merchant onboarding approval alert
    WELCOME_EMAIL,          // User account welcome & verification success
    USERNAME_REMINDER,      // Periodic reminder to customize auto-generated username
    ACCOUNT_ALERT           // Security alerts, temporary username assignments, password resets
}
