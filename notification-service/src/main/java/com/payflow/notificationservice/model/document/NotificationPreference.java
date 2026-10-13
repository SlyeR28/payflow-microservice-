package com.payflow.notificationservice.model.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notification_preferences")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationPreference {

     @Id
     private String id;

     @Indexed(unique = true)
     private Long merchantId;

     private boolean emailEnabled;

     private boolean smsEnabled;

     private boolean whatsappEnabled;

     private boolean webhookEnabled;

     private String webhookUrl;

     private String webhookSecret;

     @LastModifiedDate
     private Instant updatedAt;

}
