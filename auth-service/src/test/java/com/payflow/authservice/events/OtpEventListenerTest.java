package com.payflow.authservice.events;

import com.payflow.authservice.model.enums.OtpPurpose;
import com.payflow.authservice.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpEventListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OtpEventListener otpEventListener;

    @Test
    @DisplayName("handleOtpRequested: delegates to emailService.sendOtpEmail")
    void testHandleOtpRequestedDelegatesToEmailService() {
        OtpRequestedEvent event = new OtpRequestedEvent("test@payflow.com", "849201", OtpPurpose.EMAIL_VERIFICATION);

        otpEventListener.handleOtpRequested(event);

        verify(emailService, times(1)).sendOtpEmail("test@payflow.com", "849201", "EMAIL_VERIFICATION");
    }

    @Test
    @DisplayName("handleOtpRequested: catches and logs errors without failing listener")
    void testHandleOtpRequestedCatchesErrors() {
        OtpRequestedEvent event = new OtpRequestedEvent("test@payflow.com", "849201", OtpPurpose.EMAIL_VERIFICATION);
        doThrow(new RuntimeException("Simulated error")).when(emailService).sendOtpEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> otpEventListener.handleOtpRequested(event));
    }
}
