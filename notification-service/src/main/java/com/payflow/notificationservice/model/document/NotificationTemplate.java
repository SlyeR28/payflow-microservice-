package com.payflow.notificationservice.model.document;

import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.model.enums.TemplateType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notification_templates")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationTemplate {

    @Id
    private String id;

    @Indexed(unique = true)
    private String templateCode;

    private TemplateType templateType;

    private NotificationChannel channel;

    private String subject; //Payment successful for Order [[$order]]

    private String bodyTemplate;

    @Builder.Default
    private boolean active = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
