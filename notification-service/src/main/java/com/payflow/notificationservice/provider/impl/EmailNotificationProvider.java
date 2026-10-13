package com.payflow.notificationservice.provider.impl;

import com.payflow.notificationservice.model.document.NotificationMessages;
import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.provider.NotificationProvider;
import com.payflow.notificationservice.provider.ProviderResult;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationProvider implements NotificationProvider {

    private final JavaMailSender javaMailSender;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public ProviderResult send(NotificationMessages messages) {
        long start = System.currentTimeMillis();
        String messageId = "EML-" + UUID.randomUUID().toString().substring(0, 8);

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

            mimeMessageHelper.setTo(messages.getRecipient());
            mimeMessageHelper.setSubject(messages.getSubject() != null ? messages.getSubject() : "PayFlow Notification");
            mimeMessageHelper.setText(messages.getContent() != null ? messages.getContent() : "" , true);
            mimeMessageHelper.setFrom("notification@payflow.com");

            javaMailSender.send(mimeMessage);

            long latency  = System.currentTimeMillis() - start;

            log.info("Email Successfully Dispatched to [{}] in {}ms" , messages.getRecipient(), latency);
            return ProviderResult.success("JavaMailSender" , messageId , 200 , latency);

        } catch (Exception e) {
            long latency  = System.currentTimeMillis() - start;
            log.warn("Email dispatch failed to [{}] (using dev fallback): {}", messages.getRecipient(), e.getMessage());
            return ProviderResult.failure("JavaMailSender-simulated" , messageId , 500 , latency);
        }
    }
}
