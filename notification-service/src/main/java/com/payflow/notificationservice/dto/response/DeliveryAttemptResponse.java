package com.payflow.notificationservice.dto.response;

import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.model.enums.NotificationStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryAttemptResponse {

    private String id;
    private String notificationId;
    private String notificationReference;
    private NotificationChannel channel;
    private String providerName;
    private int attemptNumber;
    private NotificationStatus status;
    private Integer responseStatusCode;
    private String responseBody;
    private Long latencyMs;
    private Instant createdAt;
}
