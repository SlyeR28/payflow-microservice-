package com.payflow.paymentservice.dto;

import com.payflow.paymentservice.model.enums.PaymentProvider;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInitiateRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Builder.Default
    private String currency = "INR";

    @NotNull(message = "Payment provider is required")
    private PaymentProvider provider;

    @Email(message = "Invalid customer email format")
    private String customerEmail;

    private String description;

    private String idempotencyKey;

    /**
     * Optional merchant gateway credentials.
     * If null, strategy falls back to environment/system default credentials.
     */
    private GatewayCredentials credentials;
}
