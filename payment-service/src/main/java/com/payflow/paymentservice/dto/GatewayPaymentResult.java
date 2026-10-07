package com.payflow.paymentservice.dto;

import com.payflow.paymentservice.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GatewayPaymentResult {

    private boolean success;
    private String gatewayTransactionId;
    private PaymentStatus status;
    private Map<String, Object> checkoutPayload;
    private Map<String, Object> rawResponse;
    private String errorMessage;

    public static GatewayPaymentResult success(String gatewayTransactionId, PaymentStatus status, Map<String, Object> checkoutPayload) {
        return GatewayPaymentResult.builder()
                .success(true)
                .gatewayTransactionId(gatewayTransactionId)
                .status(status)
                .checkoutPayload(checkoutPayload)
                .build();
    }

    public static GatewayPaymentResult failure(String errorMessage) {
        return GatewayPaymentResult.builder()
                .success(false)
                .status(PaymentStatus.FAILED)
                .errorMessage(errorMessage)
                .build();
    }
}
