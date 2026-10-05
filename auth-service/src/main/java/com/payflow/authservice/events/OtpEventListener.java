package com.payflow.authservice.events;

import com.payflow.authservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpEventListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handleOtpRequested(OtpRequestedEvent event) {
        try {
            emailService.sendOtpEmail(event.email(), event.otp(), event.otpPurpose().name());

        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", event.email(), e.getMessage());
        }
    }
}