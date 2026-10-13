package com.payflow.notificationservice.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePreferenceRequest {

    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean whatsappEnabled;
    private boolean webhookEnabled;
    private String webhookUrl;
    private String webhookSecret;
}
