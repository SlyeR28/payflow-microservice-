package com.payflow.notificationservice.provider.impl;

import com.payflow.notificationservice.model.document.NotificationMessages;
import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.provider.NotificationProvider;
import com.payflow.notificationservice.provider.ProviderResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class SmsNotificationProvider implements NotificationProvider {


    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public ProviderResult send(NotificationMessages messages) {
        long start = System.currentTimeMillis();
        String messageId = "SMS-" + UUID.randomUUID().toString().substring(0, 8);
        // Integrate Twilio / AWS SNS SDK or mock gateway here
        log.info("Dispatching SMS to [{}] with content: {}", messages.getRecipient(), messages.getContent());
        long latency = System.currentTimeMillis() - start;
        return ProviderResult.success("Twilio-Gateway", messageId, 200, latency);
    }
}
