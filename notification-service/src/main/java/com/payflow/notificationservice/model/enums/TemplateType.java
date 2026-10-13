package com.payflow.notificationservice.model.enums;

public enum TemplateType {
    OTP_ALERT,              // Authentication and high-value payment verification
    PAYMENT_SUCCESS,        // Customer order checkout receipt
    PAYMENT_FAILED,         // Card decline / insufficient funds alert
    REFUND_PROCESSED,       // Customer refund confirmation
    MERCHANT_PAYOUT,        // Bank payout transfer notification
    KYC_STATUS_UPDATE
}
