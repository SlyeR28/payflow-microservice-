package com.payflow.notificationservice.config;

import com.payflow.notificationservice.model.document.NotificationTemplate;
import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.model.enums.TemplateType;
import com.payflow.notificationservice.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDataSeeder implements CommandLineRunner {

    private final NotificationTemplateRepository templateRepository;

    @Override
    public void run(String... args) {
        log.info("Checking and seeding default notification templates...");

        List<NotificationTemplate> defaultTemplates = List.of(
                NotificationTemplate.builder()
                        .templateCode("OTP_ALERT-EMAIL")
                        .templateType(TemplateType.OTP_ALERT)
                        .channel(NotificationChannel.EMAIL)
                        .subject("Your PayFlow Verification Code: {{otp}}")
                        .bodyTemplate("""
                                Hello,

                                Your PayFlow verification code for {{purpose}} is: {{otp}}
                                This code is valid for 5 minutes.

                                If you did not request this verification, please secure your account immediately.

                                Best regards,
                                PayFlow Security Team
                                """)
                        .active(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build(),

                NotificationTemplate.builder()
                        .templateCode("OTP_ALERT-SMS")
                        .templateType(TemplateType.OTP_ALERT)
                        .channel(NotificationChannel.SMS)
                        .subject("PayFlow OTP")
                        .bodyTemplate("Your PayFlow verification code is: {{otp}}. Valid for 5 minutes. Do not share this code with anyone.")
                        .active(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build(),

                NotificationTemplate.builder()
                        .templateCode("WELCOME_EMAIL-EMAIL")
                        .templateType(TemplateType.WELCOME_EMAIL)
                        .channel(NotificationChannel.EMAIL)
                        .subject("Welcome to PayFlow, {{username}}!")
                        .bodyTemplate("""
                                Hello {{username}},

                                Welcome to PayFlow! Your account has been successfully verified and activated.
                                You can now access your dashboard and explore payment services.

                                Best regards,
                                The PayFlow Team
                                """)
                        .active(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build(),

                NotificationTemplate.builder()
                        .templateCode("USERNAME_REMINDER-EMAIL")
                        .templateType(TemplateType.USERNAME_REMINDER)
                        .channel(NotificationChannel.EMAIL)
                        .subject("PayFlow: Remember to personalize your username")
                        .bodyTemplate("""
                                Hello,

                                You are currently using an auto-generated username: {{username}}.
                                You can customize your username at any time in your PayFlow profile settings.

                                Best regards,
                                The PayFlow Team
                                """)
                        .active(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build(),

                NotificationTemplate.builder()
                        .templateCode("ACCOUNT_ALERT-EMAIL")
                        .templateType(TemplateType.ACCOUNT_ALERT)
                        .channel(NotificationChannel.EMAIL)
                        .subject("PayFlow Security Notification")
                        .bodyTemplate("""
                                Hello,

                                {{message}}

                                Best regards,
                                PayFlow Security Team
                                """)
                        .active(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build(),

                NotificationTemplate.builder()
                        .templateCode("PAYMENT_SUCCESS-EMAIL")
                        .templateType(TemplateType.PAYMENT_SUCCESS)
                        .channel(NotificationChannel.EMAIL)
                        .subject("Payment Confirmed: {{amount}}")
                        .bodyTemplate("""
                                Hello,

                                Your payment of {{amount}} (Ref: {{transactionReference}}) has been successfully processed.

                                Thank you for using PayFlow!
                                """)
                        .active(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build(),

                NotificationTemplate.builder()
                        .templateCode("PAYMENT_FAILED-EMAIL")
                        .templateType(TemplateType.PAYMENT_FAILED)
                        .channel(NotificationChannel.EMAIL)
                        .subject("Payment Failed - {{transactionReference}}")
                        .bodyTemplate("""
                                Hello,

                                We were unable to process your payment for reference {{transactionReference}}.
                                Please check your payment method and try again.

                                PayFlow Support Team
                                """)
                        .active(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build(),

                NotificationTemplate.builder()
                        .templateCode("REFUND_PROCESSED-EMAIL")
                        .templateType(TemplateType.REFUND_PROCESSED)
                        .channel(NotificationChannel.EMAIL)
                        .subject("Refund Processed - {{transactionReference}}")
                        .bodyTemplate("""
                                Hello,

                                A refund of {{refundedAmount}} has been processed for reference {{transactionReference}}.

                                PayFlow Billing Team
                                """)
                        .active(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()
        );

        for (NotificationTemplate template : defaultTemplates) {
            if (templateRepository.findByTemplateCode(template.getTemplateCode()).isEmpty()) {
                templateRepository.save(template);
                log.info("Seeded default notification template: {}", template.getTemplateCode());
            }
        }
    }
}
