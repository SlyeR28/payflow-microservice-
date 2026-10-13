package com.payflow.notificationservice.provider.impl;

import com.payflow.notificationservice.model.document.NotificationMessages;
import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.provider.NotificationProvider;
import com.payflow.notificationservice.provider.ProviderResult;
import com.payflow.notificationservice.util.HmacUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class WebhookNotificationProvider implements NotificationProvider {


    private final RestTemplate restTemplate = new RestTemplate();


    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WEBHOOK;
    }

    @Override
    public ProviderResult send(NotificationMessages messages) {
        long start = System.currentTimeMillis();
      String targetUrl = messages.getRecipient();
      String payloadJson = messages.getContent() != null ? messages.getContent() : "{}";
      String webhookSecret = "pf_wh_secret_default_key";

      long timestamp = Instant.now().getEpochSecond();
      String signature = HmacUtil.calculateHmac(timestamp + "." + payloadJson ,webhookSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Payflow-Signature", "t="+timestamp + ",v1=" +signature);
        headers.set("X-Payflow-Delivery", messages.getNotificationReference());

        try{
            HttpEntity<String> request = new HttpEntity<>(payloadJson, headers);
            ResponseEntity<String> response  = restTemplate.postForEntity(targetUrl, request, String.class);
            long latency = System.currentTimeMillis() - start;
            return ProviderResult.success("WebhookClient", "WH-" + UUID.randomUUID().toString().substring(0, 8),
                    response.getStatusCode().value(), latency);
        } catch (Exception e) {
           long latency = System.currentTimeMillis() - start;
           log.warn("Webhook dispatch failed to [{}] (using dev fallback): {}", messages.getRecipient(), e.getMessage());
           return ProviderResult.failure("WebhookClient", e.getMessage(),
                   500, latency);
        }

    }
}
