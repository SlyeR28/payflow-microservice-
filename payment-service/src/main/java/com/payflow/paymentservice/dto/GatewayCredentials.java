package com.payflow.paymentservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GatewayCredentials {
    private String apiKey;
    private String apiSecret;
    private String webhookSecret;
}
