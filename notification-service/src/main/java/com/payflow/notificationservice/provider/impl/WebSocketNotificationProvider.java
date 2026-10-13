package com.payflow.notificationservice.provider.impl;

import com.payflow.notificationservice.model.document.NotificationMessages;
import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.provider.NotificationProvider;
import com.payflow.notificationservice.provider.ProviderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketNotificationProvider implements NotificationProvider {

   private final SimpMessagingTemplate messagingTemplate;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public ProviderResult send(NotificationMessages messages) {
        long start = System.currentTimeMillis();
        String destination =  "/topic/merchant" +messages.getMerchantId() + "/notification";

        log.info("Pushing real-time in-app notification to webSocket destination [{}]" , destination);
        messagingTemplate.convertAndSend(destination , messages);

        long latency = System.currentTimeMillis() - start;
        return ProviderResult.success("WebSocketBroker", "WS-"
                + UUID.randomUUID().toString().substring(0, 8), 200, latency);
    }
}

