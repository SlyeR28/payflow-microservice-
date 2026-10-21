package com.payflow.authservice.service;

import com.payflow.authservice.events.OtpRequestedEvent;
import com.payflow.authservice.exceptions.InvalidOtpException;
import com.payflow.authservice.exceptions.OtpExpiredException;
import com.payflow.authservice.model.entity.OtpCode;
import com.payflow.authservice.model.enums.OtpPurpose;
import com.payflow.authservice.repository.OtpCodeRepository;
import com.payflow.authservice.service.impl.OtpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private OtpCodeRepository otpCodeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "otpExpiryMinutes", 5);
    }

    private String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }

    @Test
    @DisplayName("generateAndSend: invalidates old OTPs, saves new hashed OTP and publishes event")
    void testGenerateAndSend() {
        String email = "trader@payflow.com";
        OtpPurpose purpose = OtpPurpose.EMAIL_VERIFICATION;

        otpService.generateAndSend(email, purpose);

        verify(otpCodeRepository).invalidateAll(email, purpose);
        verify(otpCodeRepository).save(any(OtpCode.class));

        ArgumentCaptor<OtpRequestedEvent> eventCaptor = ArgumentCaptor.forClass(OtpRequestedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        OtpRequestedEvent event = eventCaptor.getValue();
        assertEquals(email, event.email());
        assertEquals(purpose, event.otpPurpose());
        assertNotNull(event.otp());
        assertEquals(6, event.otp().length());
    }

    @Test
    @DisplayName("verifyOtp: successfully verifies matching OTP and marks consumed")
    void testVerifyOtpSuccess() throws Exception {
        String email = "trader@payflow.com";
        String otp = "123456";
        String hash = sha256(otp);

        OtpCode otpCode = OtpCode.builder()
                .id(1L)
                .email(email)
                .codeHash(hash)
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .consumed(false)
                .attempts(0)
                .build();

        when(otpCodeRepository.findLatestValid(eq(email), eq(OtpPurpose.EMAIL_VERIFICATION), any(Instant.class)))
                .thenReturn(Optional.of(otpCode));

        assertDoesNotThrow(() -> otpService.verifyOtp(email, otp, OtpPurpose.EMAIL_VERIFICATION));

        assertTrue(otpCode.getConsumed());
        verify(otpCodeRepository).save(otpCode);
    }

    @Test
    @DisplayName("verifyOtp: throws InvalidOtpException and increments attempts on incorrect OTP")
    void testVerifyOtpIncorrect() throws Exception {
        String email = "trader@payflow.com";
        String correctHash = sha256("999999");

        OtpCode otpCode = OtpCode.builder()
                .id(1L)
                .email(email)
                .codeHash(correctHash)
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .consumed(false)
                .attempts(1)
                .build();

        when(otpCodeRepository.findLatestValid(eq(email), eq(OtpPurpose.EMAIL_VERIFICATION), any(Instant.class)))
                .thenReturn(Optional.of(otpCode));

        assertThrows(InvalidOtpException.class, () ->
                otpService.verifyOtp(email, "000000", OtpPurpose.EMAIL_VERIFICATION));

        assertEquals(2, otpCode.getAttempts());
        verify(otpCodeRepository).save(otpCode);
    }

    @Test
    @DisplayName("verifyOtp: throws OtpExpiredException when no valid OTP exists")
    void testVerifyOtpExpired() {
        when(otpCodeRepository.findLatestValid(anyString(), any(), any(Instant.class)))
                .thenReturn(Optional.empty());

        assertThrows(OtpExpiredException.class, () ->
                otpService.verifyOtp("user@payflow.com", "123456", OtpPurpose.PASSWORD_RESET));
    }
}
