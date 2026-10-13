package com.payflow.notificationservice.model.document;

import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.model.enums.NotificationPriority;
import com.payflow.notificationservice.model.enums.NotificationStatus;
import com.payflow.notificationservice.model.enums.TemplateType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "notification_messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationMessages {


    @Id
    private String id;

    @Indexed(unique = true)
    private String notificationReference;

    @Indexed
    private String idempotencyKey;

    @Indexed
    private Long merchantId;

    private String recipient;   // email , phone , webhook ,URL or merchantId for in-app

    private NotificationChannel notificationChannel;

    private TemplateType templateType;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    private String subject;

    private String content;

    @Builder.Default
    private Map<String , Object> payload = new HashMap<>();

    @Builder.Default
    private int retryCount = 0;

    @Builder.Default
    private int maxRetries = 3;

    private String failureReason;

    private String providerMessageId;

    private Instant sentAt;

    private  Instant deliveredAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

}
