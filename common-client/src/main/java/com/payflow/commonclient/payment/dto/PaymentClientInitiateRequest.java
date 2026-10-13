package com.payflow.commonclient.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentClientInitiateRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01" , message = "Amount must be greater than 0.01")
    private BigDecimal amount;

    @Builder.Default
    private String currency = "INR";

    @NotNull(message = "Provider is required")
    private String provider;

    private String customerEmail;

    private String description;

    private String idempotencyKey;

    /**
     * Optional custom gateway credentials map (apiKey, apiSecret, webhookSecret)
     */
    private Map<String, String> customGatewayCredentials;

}
