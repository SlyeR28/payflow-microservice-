package com.payflow.commonclient.payment;

import com.payflow.common.dto.ApiResponse;
import com.payflow.commonclient.config.FeignConfig;
import com.payflow.commonclient.payment.dto.PaymentClientInitiateRequest;
import com.payflow.commonclient.payment.dto.PaymentClientResponse;
import com.payflow.commonclient.payment.dto.RefundClientRequest;
import com.payflow.commonclient.payment.dto.RefundClientResponse;
import com.payflow.commonclient.payment.fallback.PaymentServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", configuration = FeignConfig.class, fallbackFactory = PaymentServiceFallbackFactory.class)
public interface PaymentServiceClient {

        @PostMapping("/api/v1/payments/initiate")
        ApiResponse<PaymentClientResponse> initiatePayment(@RequestBody PaymentClientInitiateRequest request);

        @GetMapping("/api/v1/payments/{id}")
        ApiResponse<PaymentClientResponse> getTransactionById(@PathVariable("id") Long id);

        @GetMapping("/api/v1/payments/ref/{reference}")
        ApiResponse<PaymentClientResponse> getTransactionByReference(@PathVariable("reference") String reference);

        @PostMapping("/api/v1/payments/{reference}/refund")
        ApiResponse<RefundClientResponse> refundPayment(
                        @PathVariable("reference") String reference, @RequestBody RefundClientRequest request);
}
