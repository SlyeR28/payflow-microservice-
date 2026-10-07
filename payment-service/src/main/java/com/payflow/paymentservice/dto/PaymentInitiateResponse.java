package com.payflow.paymentservice.dto;

import com.payflow.paymentservice.model.enums.PaymentProvider;
import com.payflow.paymentservice.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInitiateResponse {

    private Long id;
    private String transactionReference;
    private PaymentProvider provider;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String gatewayTransactionId;
    private String customerEmail;
    private String description;
    private String idempotencyKey;
    private String failureReason;
    private Map<String, Object> checkoutPayload;
    private Instant createdAt;
    private Instant updatedAt;
}
