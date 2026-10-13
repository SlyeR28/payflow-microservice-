package com.payflow.paymentservice.dto;

import com.payflow.paymentservice.model.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundResponse {

    private String transactionReference;
    private String gatewayRefundId;
    private BigDecimal refundedAmount;
    private PaymentStatus status;
    private String message;
    private Instant refundedAt;
}
