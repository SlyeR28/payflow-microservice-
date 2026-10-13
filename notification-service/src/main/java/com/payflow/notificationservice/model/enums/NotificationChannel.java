package com.payflow.notificationservice.model.enums;

public enum NotificationChannel {
    EMAIL,       // Customer receipts, invoices, onboarding
    SMS,         // OTPs, urgent payment status alerts
    WHATSAPP,    // Interactive receipts & customer support alerts
    WEBHOOK,     // Cryptographically signed HTTP POST to merchant servers
    IN_APP       // Real-time WebSocket push to merchant dashboard
}
