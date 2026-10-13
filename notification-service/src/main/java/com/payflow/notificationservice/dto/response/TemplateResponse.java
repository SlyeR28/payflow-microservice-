package com.payflow.notificationservice.dto.response;

import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.model.enums.TemplateType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TemplateResponse {

    private String id;
    private String templateCode;
    private TemplateType templateType;
    private NotificationChannel channel;
    private String subject;
    private String bodyTemplate;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
