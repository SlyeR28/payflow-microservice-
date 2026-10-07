package com.payflow.paymentservice.dto;

import com.payflow.paymentservice.model.enums.PaymentProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyCredentialsRequest {

    @NotNull(message = "Payment provider is required")
    private PaymentProvider provider;

    @NotBlank(message = "API key or Key ID is required")
    private String apiKey;

    private String apiSecret;

    private String webhookSecret;
}
