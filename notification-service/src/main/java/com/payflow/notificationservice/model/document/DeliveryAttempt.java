package com.payflow.notificationservice.model.document;

import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.model.enums.NotificationStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "delivery_attempts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryAttempt {

    @Id
    private String id;

    @Indexed
    private String notificationId;

    private String notificationReference;

    private NotificationChannel channel;

    private String providerName; // send grid , AWS_SES , Twilio ,webhookClient

    private int attemptNumber;

    private NotificationStatus status;

    private Integer responseStatusCode;  // 200 ,429 ,500

    private String responseBody;

    private Long latencyMs;

    @Indexed
    @CreatedDate
    private Instant createdAt;


}
