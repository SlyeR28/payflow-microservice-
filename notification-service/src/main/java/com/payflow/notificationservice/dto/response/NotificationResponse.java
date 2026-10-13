package com.payflow.notificationservice.dto.response;

import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.model.enums.NotificationPriority;
import com.payflow.notificationservice.model.enums.NotificationStatus;
import com.payflow.notificationservice.model.enums.TemplateType;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {

    private String id;
    private String notificationReference;
    private String idempotencyKey;
    private Long merchantId;
    private String recipient;
    private NotificationChannel channel;
    private TemplateType templateType;
    private NotificationPriority priority;
    private NotificationStatus status;
    private String subject;
    private String content;
    private Map<String, Object> payload;
    private int retryCount;
    private int maxRetries;
    private String failureReason;
    private String providerMessageId;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant createdAt;
    private Instant updatedAt;
}
