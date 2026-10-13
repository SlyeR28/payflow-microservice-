package com.payflow.commonclient.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundClientResponse {

    private String transactionReference;
    private String gatewayRefundId;
    private BigDecimal refundedAmount;
    private String status;
    private String message;
    private Instant refundedAt;

}
