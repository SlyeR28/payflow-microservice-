package com.payflow.paymentservice.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class RazorpayPaymentStrategyMockTest {

    private MockRestServiceServer mockServer;
    private RazorpayPaymentStrategy strategy;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        strategy = new RazorpayPaymentStrategy(builder, "https://api.razorpay.com", objectMapper);
    }

    @Test
    @DisplayName("Should verify valid Razorpay sandbox credentials via Basic Auth RestClient")
    void shouldVerifyValidRazorpayCredentials() {
        String keyId = "rzp_test_mockKeyId123";
        String keySecret = "secret_mockSecret456";
        String expectedAuth = "Basic " + Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));

        String mockPaymentsJson = """
                {
                    "entity": "collection",
                    "count": 1,
                    "items": []
                }
                """;

        mockServer.expect(requestTo("https://api.razorpay.com/v1/payments?count=1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, expectedAuth))
                .andRespond(withSuccess(mockPaymentsJson, MediaType.APPLICATION_JSON));

        GatewayCredentials credentials = GatewayCredentials.builder()
                .apiKey(keyId)
                .apiSecret(keySecret)
                .build();

        GatewayVerificationResponse response = strategy.verifyCredentials(credentials);

        mockServer.verify();
        assertThat(response.isValid()).isTrue();
        assertThat(response.getProvider()).isEqualTo(PaymentProvider.RAZORPAY);
        assertThat(response.getMessage()).contains("verified successfully");
        assertThat(response.getAccountDetails()).containsEntry("entity", "collection");
        assertThat(response.getAccountDetails()).containsEntry("status", "ACTIVE");
    }

    @Test
    @DisplayName("Should handle 401 Unauthorized with granular Razorpay error description")
    void shouldHandleRazorpay401UnauthorizedGranularly() {
        String badKey = "rzp_test_badKey";
        String badSecret = "bad_secret";
        String expectedAuth = "Basic " + Base64.getEncoder().encodeToString((badKey + ":" + badSecret).getBytes(StandardCharsets.UTF_8));

        String errorJson = """
                {
                    "error": {
                        "code": "BAD_REQUEST_ERROR",
                        "description": "The id provided does not exist",
                        "source": "business",
                        "step": "payment_initiation",
                        "reason": "input_validation_failed"
                    }
                }
                """;

        mockServer.expect(requestTo("https://api.razorpay.com/v1/payments?count=1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, expectedAuth))
                .andRespond(withUnauthorizedRequest().body(errorJson).contentType(MediaType.APPLICATION_JSON));

        GatewayCredentials credentials = GatewayCredentials.builder()
                .apiKey(badKey)
                .apiSecret(badSecret)
                .build();

        GatewayVerificationResponse response = strategy.verifyCredentials(credentials);

        mockServer.verify();
        assertThat(response.isValid()).isFalse();
        assertThat(response.getProvider()).isEqualTo(PaymentProvider.RAZORPAY);
        assertThat(response.getMessage()).contains("The id provided does not exist");
    }

    @Test
    @DisplayName("Should create Razorpay order via RestClient POST")
    void shouldCreateRazorpayOrder() {
        String keyId = "rzp_test_orderKey";
        String keySecret = "secret_orderSecret";
        String expectedAuth = "Basic " + Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));

        String orderJson = """
                {
                    "id": "order_EKwxwAgItmmMnC",
                    "entity": "order",
                    "amount": 50000,
                    "currency": "INR",
                    "receipt": "TXN_RZP_001",
                    "status": "created"
                }
                """;

        mockServer.expect(requestTo("https://api.razorpay.com/v1/orders"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, expectedAuth))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(orderJson, MediaType.APPLICATION_JSON));

        PaymentTransaction txn = PaymentTransaction.builder()
                .transactionReference("TXN_RZP_001")
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .customerEmail("test@example.com")
                .build();

        GatewayCredentials credentials = GatewayCredentials.builder()
                .apiKey(keyId)
                .apiSecret(keySecret)
                .build();

        GatewayPaymentResult result = strategy.initiatePayment(txn, credentials);

        mockServer.verify();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getGatewayTransactionId()).isEqualTo("order_EKwxwAgItmmMnC");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(result.getCheckoutPayload()).containsEntry("orderId", "order_EKwxwAgItmmMnC");
        assertThat(result.getCheckoutPayload()).containsEntry("keyId", keyId);
    }

    @Test
    @DisplayName("Should issue Razorpay refund via RestClient POST")
    void shouldIssueRazorpayRefund() {
        String keyId = "rzp_test_refundKey";
        String keySecret = "secret_refundSecret";
        String expectedAuth = "Basic " + Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));

        String refundJson = """
                {
                    "id": "rfnd_FP8uX0pQy7Vw2k",
                    "entity": "refund",
                    "amount": 50000,
                    "currency": "INR",
                    "payment_id": "pay_EKwxwAgItmmMnC",
                    "status": "processed"
                }
                """;

        mockServer.expect(requestTo("https://api.razorpay.com/v1/payments/pay_EKwxwAgItmmMnC/refund"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, expectedAuth))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(refundJson, MediaType.APPLICATION_JSON));

        PaymentTransaction txn = PaymentTransaction.builder()
                .transactionReference("TXN_RZP_REFUND_001")
                .gatewayTransactionId("pay_EKwxwAgItmmMnC")
                .amount(new BigDecimal("500.00"))
                .build();

        GatewayCredentials credentials = GatewayCredentials.builder()
                .apiKey(keyId)
                .apiSecret(keySecret)
                .build();

        GatewayRefundResult result = strategy.refundPayment(txn, new BigDecimal("500.00"), "Product defect", credentials);

        mockServer.verify();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getGatewayRefundId()).isEqualTo("rfnd_FP8uX0pQy7Vw2k");
    }
}
