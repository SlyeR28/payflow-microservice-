package com.payflow.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GatewayRefundResult {

    private boolean success;
    private String gatewayRefundId;
    private BigDecimal refundedAmount;
    private Map<String, Object> rawResponse;
    private String errorMessage;

    public static GatewayRefundResult success(String gatewayRefundId, BigDecimal refundedAmount) {
        return GatewayRefundResult.builder()
                .success(true)
                .gatewayRefundId(gatewayRefundId)
                .refundedAmount(refundedAmount)
                .build();
    }

    public static GatewayRefundResult failure(String errorMessage) {
        return GatewayRefundResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
