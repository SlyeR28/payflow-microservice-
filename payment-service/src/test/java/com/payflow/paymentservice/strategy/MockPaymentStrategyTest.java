package com.payflow.paymentservice.strategy;

import com.payflow.paymentservice.dto.GatewayCredentials;
import com.payflow.paymentservice.dto.GatewayPaymentResult;
import com.payflow.paymentservice.dto.GatewayRefundResult;
import com.payflow.paymentservice.dto.GatewayVerificationResponse;
import com.payflow.paymentservice.model.entity.PaymentTransaction;
import com.payflow.paymentservice.model.enums.PaymentProvider;
import com.payflow.paymentservice.model.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MockPaymentStrategyTest {

    private MockPaymentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new MockPaymentStrategy();
    }

    @Test
    @DisplayName("Should return MOCK provider")
    void shouldReturnMockProvider() {
        assertThat(strategy.getProvider()).isEqualTo(PaymentProvider.MOCK);
    }

    @Test
    @DisplayName("Should verify valid mock test API key")
    void shouldVerifyValidMockKey() {
        GatewayCredentials credentials = GatewayCredentials.builder()
                .apiKey("test_mock_secret_key_123")
                .build();

        GatewayVerificationResponse response = strategy.verifyCredentials(credentials);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getProvider()).isEqualTo(PaymentProvider.MOCK);
        assertThat(response.getAccountDetails()).containsKey("status");
        assertThat(response.getAccountDetails().get("status")).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should fail verification for empty key")
    void shouldFailVerificationForEmptyKey() {
        GatewayCredentials credentials = GatewayCredentials.builder()
                .apiKey("")
                .build();

        GatewayVerificationResponse response = strategy.verifyCredentials(credentials);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).contains("Mock API key cannot be empty");
    }

    @Test
    @DisplayName("Should fail verification for invalid prefix key")
    void shouldFailVerificationForInvalidPrefix() {
        GatewayCredentials credentials = GatewayCredentials.builder()
                .apiKey("invalid_key_456")
                .build();

        GatewayVerificationResponse response = strategy.verifyCredentials(credentials);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).contains("rejected");
    }

    @Test
    @DisplayName("Should initiate mock payment successfully")
    void shouldInitiateMockPayment() {
        PaymentTransaction txn = PaymentTransaction.builder()
                .transactionReference("TXN_MOCK_TEST_001")
                .amount(new BigDecimal("999.00"))
                .currency("INR")
                .build();

        GatewayPaymentResult result = strategy.initiatePayment(txn, null);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(result.getGatewayTransactionId()).startsWith("mock_order_");
        assertThat(result.getCheckoutPayload()).containsKey("checkoutUrl");
    }

    @Test
    @DisplayName("Should simulate refund successfully")
    void shouldSimulateRefund() {
        PaymentTransaction txn = PaymentTransaction.builder()
                .transactionReference("TXN_MOCK_TEST_002")
                .gatewayTransactionId("mock_order_12345")
                .amount(new BigDecimal("500.00"))
                .build();

        GatewayRefundResult result = strategy.refundPayment(txn, new BigDecimal("250.00"), "Product returned", null);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getGatewayRefundId()).startsWith("mock_rfnd_");
        assertThat(result.getRefundedAmount()).isEqualByComparingTo("250.00");
    }
}
