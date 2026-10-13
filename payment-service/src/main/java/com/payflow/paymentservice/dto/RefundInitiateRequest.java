package com.payflow.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundInitiateRequest {

    @DecimalMin(value = "0.01", message = "Refund amount must be greater than zero")
    private BigDecimal amount;

    private String reason;

    private GatewayCredentials credentials;
}
