package com.payflow.commonclient.payment.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentClientResponse {


    private long id;
    private Long merchantId;
    private String transactionReference;
    private String provider;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String gatewayTransactionId;
    private String customerEmail;
    private String description;
    private String idempotencyKey;
    private String failedReason;
    private Map<String, Object> checkOutPayload;
    private Instant createdAt;
    private Instant updatedAt;

}
