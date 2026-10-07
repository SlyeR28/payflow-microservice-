package com.payflow.paymentservice.strategy;

import com.payflow.paymentservice.dto.GatewayCredentials;
import com.payflow.paymentservice.dto.GatewayPaymentResult;
import com.payflow.paymentservice.dto.GatewayRefundResult;
import com.payflow.paymentservice.dto.GatewayVerificationResponse;
import com.payflow.paymentservice.model.entity.PaymentTransaction;
import com.payflow.paymentservice.model.enums.PaymentProvider;
import com.payflow.paymentservice.model.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class MockPaymentStrategy implements PaymentGatewayStrategy {

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.MOCK;
    }

    @Override
    public GatewayVerificationResponse verifyCredentials(GatewayCredentials credentials) {
        log.info("Verifying mock gateway credentials");
        if (credentials == null || credentials.getApiKey() == null || credentials.getApiKey().isBlank()) {
            return GatewayVerificationResponse.failure(PaymentProvider.MOCK, "Mock API key cannot be empty");
        }

        String key = credentials.getApiKey().trim();
        if (key.equalsIgnoreCase("invalid") || key.startsWith("invalid_")) {
            return GatewayVerificationResponse.failure(PaymentProvider.MOCK, "Mock gateway rejected test credentials");
        }

        Map<String, Object> details = new HashMap<>();
        details.put("environment", "mock-sandbox");
        details.put("mode", "simulation");
        details.put("status", "ACTIVE");
        details.put("merchantId", "mock_mch_" + key.hashCode());

        return GatewayVerificationResponse.success(
                PaymentProvider.MOCK,
                "Mock gateway credentials verified successfully in local simulation mode",
                details
        );
    }

    @Override
    public GatewayPaymentResult initiatePayment(PaymentTransaction transaction, GatewayCredentials credentials) {
        log.info("Initiating simulated payment for transaction: {}", transaction.getTransactionReference());
        String mockTxnId = "mock_order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Map<String, Object> payload = new HashMap<>();
        payload.put("gateway", "MOCK");
        payload.put("orderId", mockTxnId);
        payload.put("checkoutUrl", "https://mock-gateway.payflow.local/checkout/" + mockTxnId);
        payload.put("amount", transaction.getAmount());
        payload.put("currency", transaction.getCurrency());

        return GatewayPaymentResult.success(mockTxnId, PaymentStatus.AUTHORIZED, payload);
    }

    @Override
    public GatewayRefundResult refundPayment(PaymentTransaction transaction, BigDecimal refundAmount, String reason, GatewayCredentials credentials) {
        log.info("Simulating refund of {} for txn: {}", refundAmount, transaction.getTransactionReference());
        String refundId = "mock_rfnd_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return GatewayRefundResult.success(refundId, refundAmount);
    }
}
