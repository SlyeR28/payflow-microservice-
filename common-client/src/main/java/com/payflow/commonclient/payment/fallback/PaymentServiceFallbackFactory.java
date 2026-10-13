package com.payflow.commonclient.payment.fallback;

import com.payflow.common.dto.ApiResponse;
import com.payflow.commonclient.payment.PaymentServiceClient;
import com.payflow.commonclient.payment.dto.PaymentClientInitiateRequest;
import com.payflow.commonclient.payment.dto.PaymentClientResponse;
import com.payflow.commonclient.payment.dto.RefundClientRequest;
import com.payflow.commonclient.payment.dto.RefundClientResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentServiceFallbackFactory implements FallbackFactory<PaymentServiceClient> {

    @Override
    public PaymentServiceClient create(Throwable cause) {
        return new PaymentServiceClient() {

            @Override
            public ApiResponse<PaymentClientResponse> initiatePayment(PaymentClientInitiateRequest request) {
                log.error("PaymentServiceFallback: initiatePayment failed. Cause: {}", cause.getMessage());
                return ApiResponse.<PaymentClientResponse>builder()
                        .success(false)
                        .message("Payment service is temporarily unavailable. Please retry shortly.")
                        .build();
            }

            @Override
            public ApiResponse<PaymentClientResponse> getTransactionById(Long id) {
                log.error("PaymentServiceFallback: getTransactionById failed for id {}. Cause: {}", id, cause.getMessage());
                return ApiResponse.<PaymentClientResponse>builder()
                        .success(false)
                        .message("Payment transaction lookup is currently unavailable.")
                        .build();
            }

            @Override
            public ApiResponse<PaymentClientResponse> getTransactionByReference(String reference) {
                log.error("PaymentServiceFallback: getTransactionByReference failed for ref {}. Cause: {}", reference, cause.getMessage());
                return ApiResponse.<PaymentClientResponse>builder()
                        .success(false)
                        .message("Payment transaction lookup is currently unavailable.")
                        .build();
            }

            @Override
            public ApiResponse<RefundClientResponse> refundPayment(String reference, RefundClientRequest request) {
                log.error("PaymentServiceFallback: refundPayment failed for ref {}. Cause: {}", reference, cause.getMessage());
                return ApiResponse.<RefundClientResponse>builder()
                        .success(false)
                        .message("Refund processing is temporarily unavailable.")
                        .build();
            }
        };
    }
}
