package com.payflow.commonclient.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationClientRequest {

    private String idempotencyKey;
    private Long merchantId;
    private String recipient;
    private String channel;
    private String templateType;
    @Builder.Default
    private String priority = "MEDIUM";
    private String subject;
    private Map<String , Object> payload;
}
