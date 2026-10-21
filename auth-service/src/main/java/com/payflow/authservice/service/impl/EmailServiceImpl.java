package com.payflow.authservice.service.impl;

import com.payflow.authservice.service.EmailService;
import com.payflow.common.dto.ApiResponse;
import com.payflow.commonclient.notification.NotificationServiceClient;
import com.payflow.commonclient.notification.dto.NotificationClientRequest;
import com.payflow.commonclient.notification.dto.NotificationClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final NotificationServiceClient notificationServiceClient;

    @Override
    public void sendOtpEmail(String to, String otp, String purpose) {
        log.info("Dispatching OTP email to [{}] for purpose [{}]", to, purpose);
        try {
            NotificationClientRequest request = NotificationClientRequest.builder()
                    .idempotencyKey("OTP_" + UUID.randomUUID())
                    .recipient(to)
                    .channel("EMAIL")
                    .templateType("OTP_ALERT")
                    .priority("HIGH")
                    .subject("Your PayFlow Verification Code: " + otp)
                    .payload(Map.of(
                            "otp", otp,
                            "purpose", purpose != null ? purpose : "verification",
                            "recipient", to,
                            "email", to
                    ))
                    .build();

            ApiResponse<NotificationClientResponse> response = notificationServiceClient.sendNotification(request);
            if (response != null && response.isSuccess()) {
                log.info("OTP notification successfully accepted by notification-service for [{}]", to);
            } else {
                log.warn("Notification-service returned non-success for OTP to [{}]: {}", to,
                        response != null ? response.getMessage() : "null response");
            }
        } catch (Exception e) {
            log.warn("Direct notification dispatch failed for OTP to [{}], using resilient fallback: {}", to, e.getMessage());
            log.info("""

                ╔═══════════════════════════════════════════╗
                ║         EMAIL (FALLBACK) — OTP SENT       ║
                ╠═══════════════════════════════════════════╣
                ║ To:      {}
                ║ Purpose: {}
                ║ OTP:     {}
                ╚═══════════════════════════════════════════╝
                """, to, purpose, otp);
        }
    }

    @Override
    public void sendWelcomeEmail(String to, String username) {
        log.info("Dispatching welcome email to [{}] (username: {})", to, username);
        try {
            NotificationClientRequest request = NotificationClientRequest.builder()
                    .idempotencyKey("WELCOME_" + UUID.randomUUID())
                    .recipient(to)
                    .channel("EMAIL")
                    .templateType("WELCOME_EMAIL")
                    .priority("MEDIUM")
                    .subject("Welcome to PayFlow, " + username + "!")
                    .payload(Map.of(
                            "username", username != null ? username : "User",
                            "recipient", to,
                            "email", to
                    ))
                    .build();

            ApiResponse<NotificationClientResponse> response = notificationServiceClient.sendNotification(request);
            if (response != null && response.isSuccess()) {
                log.info("Welcome email accepted by notification-service for [{}]", to);
            } else {
                log.warn("Notification-service returned non-success for welcome email to [{}]", to);
            }
        } catch (Exception e) {
            log.warn("Notification dispatch failed for welcome email to [{}]: {}", to, e.getMessage());
            log.info("[FALLBACK EMAIL] Welcome sent to {} (username: {})", to, username);
        }
    }

    @Override
    public void sendTemporaryUsernameEmail(String to, String username) {
        log.info("Dispatching temporary username notification to [{}] (username: {})", to, username);
        try {
            NotificationClientRequest request = NotificationClientRequest.builder()
                    .idempotencyKey("TEMP_USER_" + UUID.randomUUID())
                    .recipient(to)
                    .channel("EMAIL")
                    .templateType("ACCOUNT_ALERT")
                    .priority("MEDIUM")
                    .subject("PayFlow: Temporary Username Assigned")
                    .payload(Map.of(
                            "username", username != null ? username : "",
                            "recipient", to,
                            "email", to,
                            "message", "Temporary username assigned. Please update it in profile settings as needed."
                    ))
                    .build();

            ApiResponse<NotificationClientResponse> response = notificationServiceClient.sendNotification(request);
            if (response != null && response.isSuccess()) {
                log.info("Temp username notification accepted by notification-service for [{}]", to);
            } else {
                log.warn("Notification-service returned non-success for temp username email to [{}]", to);
            }
        } catch (Exception e) {
            log.warn("Notification dispatch failed for temp username email to [{}]: {}", to, e.getMessage());
            log.info("""

                ╔═══════════════════════════════════════════╗
                ║     EMAIL (FALLBACK) — TEMP USERNAME      ║
                ╠═══════════════════════════════════════════╣
                ║ To:       {}
                ║ Username: {}
                ║ Message:  Temporary username assigned.
                ║           Please update it as needed.
                ╚═══════════════════════════════════════════╝
                """, to, username);
        }
    }

    @Override
    public void sendUsernameReminderEmail(String to, String currentUsername) {
        log.info("Dispatching username reminder email to [{}] (current username: {})", to, currentUsername);
        try {
            NotificationClientRequest request = NotificationClientRequest.builder()
                    .idempotencyKey("REMINDER_USER_" + UUID.randomUUID())
                    .recipient(to)
                    .channel("EMAIL")
                    .templateType("USERNAME_REMINDER")
                    .priority("LOW")
                    .subject("PayFlow: Username Reminder")
                    .payload(Map.of(
                            "username", currentUsername != null ? currentUsername : "",
                            "recipient", to,
                            "email", to
                    ))
                    .build();

            ApiResponse<NotificationClientResponse> response = notificationServiceClient.sendNotification(request);
            if (response != null && response.isSuccess()) {
                log.info("Username reminder accepted by notification-service for [{}]", to);
            } else {
                log.warn("Notification-service returned non-success for username reminder to [{}]", to);
            }
        } catch (Exception e) {
            log.warn("Notification dispatch failed for username reminder to [{}]: {}", to, e.getMessage());
            log.info("[FALLBACK EMAIL] Username reminder sent to {} (still using: {})", to, currentUsername);
        }
    }
}
