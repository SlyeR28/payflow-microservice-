package com.payflow.authservice.service;

import com.payflow.authservice.service.impl.EmailServiceImpl;
import com.payflow.common.dto.ApiResponse;
import com.payflow.commonclient.notification.NotificationServiceClient;
import com.payflow.commonclient.notification.dto.NotificationClientRequest;
import com.payflow.commonclient.notification.dto.NotificationClientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private EmailServiceImpl emailService;

    private NotificationClientResponse clientResponse;

    @BeforeEach
    void setUp() {
        clientResponse = NotificationClientResponse.builder()
                .notificationReference("NOTIF-TEST-123")
                .status("SENT")
                .build();
    }

    @Test
    @DisplayName("sendOtpEmail: dispatches OTP request to NotificationServiceClient successfully")
    void testSendOtpEmailSuccess() {
        when(notificationServiceClient.sendNotification(any(NotificationClientRequest.class)))
                .thenReturn(ApiResponse.<NotificationClientResponse>builder()
                        .success(true)
                        .data(clientResponse)
                        .build());

        emailService.sendOtpEmail("user@example.com", "123456", "EMAIL_VERIFICATION");

        ArgumentCaptor<NotificationClientRequest> captor = ArgumentCaptor.forClass(NotificationClientRequest.class);
        verify(notificationServiceClient, times(1)).sendNotification(captor.capture());

        NotificationClientRequest captured = captor.getValue();
        assertEquals("user@example.com", captured.getRecipient());
        assertEquals("EMAIL", captured.getChannel());
        assertEquals("OTP_ALERT", captured.getTemplateType());
        assertEquals("HIGH", captured.getPriority());
        assertTrue(captured.getSubject().contains("123456"));
        assertEquals("123456", captured.getPayload().get("otp"));
        assertEquals("EMAIL_VERIFICATION", captured.getPayload().get("purpose"));
    }

    @Test
    @DisplayName("sendOtpEmail: handles feign exception gracefully without throwing")
    void testSendOtpEmailHandlesExceptionGracefully() {
        when(notificationServiceClient.sendNotification(any(NotificationClientRequest.class)))
                .thenThrow(new RuntimeException("Notification service unreachable"));

        assertDoesNotThrow(() ->
                emailService.sendOtpEmail("user@example.com", "654321", "PASSWORD_RESET"));

        verify(notificationServiceClient, times(1)).sendNotification(any(NotificationClientRequest.class));
    }

    @Test
    @DisplayName("sendWelcomeEmail: dispatches welcome email to NotificationServiceClient")
    void testSendWelcomeEmailSuccess() {
        when(notificationServiceClient.sendNotification(any(NotificationClientRequest.class)))
                .thenReturn(ApiResponse.<NotificationClientResponse>builder()
                        .success(true)
                        .data(clientResponse)
                        .build());

        emailService.sendWelcomeEmail("user@example.com", "alice_merchant");

        ArgumentCaptor<NotificationClientRequest> captor = ArgumentCaptor.forClass(NotificationClientRequest.class);
        verify(notificationServiceClient).sendNotification(captor.capture());

        NotificationClientRequest captured = captor.getValue();
        assertEquals("user@example.com", captured.getRecipient());
        assertEquals("WELCOME_EMAIL", captured.getTemplateType());
        assertEquals("alice_merchant", captured.getPayload().get("username"));
    }

    @Test
    @DisplayName("sendTemporaryUsernameEmail: dispatches temp username notification")
    void testSendTemporaryUsernameEmailSuccess() {
        when(notificationServiceClient.sendNotification(any(NotificationClientRequest.class)))
                .thenReturn(ApiResponse.<NotificationClientResponse>builder()
                        .success(true)
                        .data(clientResponse)
                        .build());

        emailService.sendTemporaryUsernameEmail("user@example.com", "temp_user_99");

        ArgumentCaptor<NotificationClientRequest> captor = ArgumentCaptor.forClass(NotificationClientRequest.class);
        verify(notificationServiceClient).sendNotification(captor.capture());

        NotificationClientRequest captured = captor.getValue();
        assertEquals("user@example.com", captured.getRecipient());
        assertEquals("ACCOUNT_ALERT", captured.getTemplateType());
        assertEquals("temp_user_99", captured.getPayload().get("username"));
    }

    @Test
    @DisplayName("sendUsernameReminderEmail: dispatches username reminder")
    void testSendUsernameReminderEmailSuccess() {
        when(notificationServiceClient.sendNotification(any(NotificationClientRequest.class)))
                .thenReturn(ApiResponse.<NotificationClientResponse>builder()
                        .success(true)
                        .data(clientResponse)
                        .build());

        emailService.sendUsernameReminderEmail("user@example.com", "temp_user_99");

        ArgumentCaptor<NotificationClientRequest> captor = ArgumentCaptor.forClass(NotificationClientRequest.class);
        verify(notificationServiceClient).sendNotification(captor.capture());

        NotificationClientRequest captured = captor.getValue();
        assertEquals("user@example.com", captured.getRecipient());
        assertEquals("USERNAME_REMINDER", captured.getTemplateType());
        assertEquals("temp_user_99", captured.getPayload().get("username"));
    }
}
