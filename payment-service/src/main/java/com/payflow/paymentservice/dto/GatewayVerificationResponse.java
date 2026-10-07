package com.payflow.paymentservice.dto;

import com.payflow.paymentservice.model.enums.PaymentProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GatewayVerificationResponse {

    private PaymentProvider provider;
    private boolean valid;
    private String message;
    private Map<String, Object> accountDetails;
    private Instant verifiedAt;

    public static GatewayVerificationResponse success(PaymentProvider provider, String message, Map<String, Object> details) {
        return GatewayVerificationResponse.builder()
                .provider(provider)
                .valid(true)
                .message(message)
                .accountDetails(details)
                .verifiedAt(Instant.now())
                .build();
    }

    public static GatewayVerificationResponse failure(PaymentProvider provider, String message) {
        return GatewayVerificationResponse.builder()
                .provider(provider)
                .valid(false)
                .message(message)
                .verifiedAt(Instant.now())
                .build();
    }
}
