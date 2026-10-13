package com.payflow.notificationservice.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PreferenceResponse {

    private String id;
    private Long merchantId;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean whatsappEnabled;
    private boolean webhookEnabled;
    private String webhookUrl;
    private Instant updatedAt;
}
