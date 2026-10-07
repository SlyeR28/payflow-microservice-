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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class StripePaymentStrategyMockTest {

    private MockRestServiceServer mockServer;
    private StripePaymentStrategy strategy;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        strategy = new StripePaymentStrategy(builder, "https://api.stripe.com", objectMapper);
    }

    @Test
    @DisplayName("Should verify valid Stripe test credentials successfully via RestClient")
    void shouldVerifyValidStripeCredentials() {
        String validApiKey = "sk_test_51MockValidKey123456789";
        String mockResponseJson = """
                {
                    "object": "balance",
                    "available": [{"amount": 500000, "currency": "usd"}],
                    "livemode": false,
                    "pending": [{"amount": 0, "currency": "usd"}]
                }
                """;

        mockServer.expect(requestTo("https://api.stripe.com/v1/balance"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + validApiKey))
                .andRespond(withSuccess(mockResponseJson, MediaType.APPLICATION_JSON));

        GatewayCredentials credentials = GatewayCredentials.builder()
                .apiKey(validApiKey)
                .build();

        GatewayVerificationResponse response = strategy.verifyCredentials(credentials);

        mockServer.verify();
        assertThat(response.isValid()).isTrue();
        assertThat(response.getProvider()).isEqualTo(PaymentProvider.STRIPE);
        assertThat(response.getMessage()).contains("verified successfully");
        assertThat(response.getAccountDetails()).containsEntry("status", "ACTIVE");
        assertThat(response.getAccountDetails()).containsEntry("livemode", false);
    }

    @Test
    @DisplayName("Should handle 401 Unauthorized with granular Stripe error message")
    void shouldHandle401UnauthorizedGranularly() {
        String invalidKey = "sk_test_invalid_fake_key_999";
        String errorJson = """
                {
                    "error": {
                        "message": "Invalid API Key provided: sk_test_invalid_fake_key_999",
                        "type": "invalid_request_error"
                    }
                }
                """;

        mockServer.expect(requestTo("https://api.stripe.com/v1/balance"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidKey))
                .andRespond(withUnauthorizedRequest().body(errorJson).contentType(MediaType.APPLICATION_JSON));

        GatewayCredentials credentials = GatewayCredentials.builder()
                .apiKey(invalidKey)
                .build();

        GatewayVerificationResponse response = strategy.verifyCredentials(credentials);

        mockServer.verify();
        assertThat(response.isValid()).isFalse();
        assertThat(response.getProvider()).isEqualTo(PaymentProvider.STRIPE);
        assertThat(response.getMessage()).contains("Invalid API Key provided: sk_test_invalid_fake_key_999");
    }

    @Test
    @DisplayName("Should handle Stripe 500 Server Error gracefully")
    void shouldHandleServerErrorGracefully() {
        String testKey = "sk_test_server_err_key";

        mockServer.expect(requestTo("https://api.stripe.com/v1/balance"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        GatewayCredentials credentials = GatewayCredentials.builder()
                .apiKey(testKey)
                .build();

        GatewayVerificationResponse response = strategy.verifyCredentials(credentials);

        mockServer.verify();
        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).contains("Stripe API returned 5xx server error");
    }

    @Test
    @DisplayName("Should initiate PaymentIntent via RestClient POST")
    void shouldInitiatePaymentIntent() {
        String apiKey = "sk_test_payment_key";
        String stripeIntentJson = """
                {
                    "id": "pi_3MtwBwLkdIwHu7ix28a3tqPa",
                    "object": "payment_intent",
                    "amount": 250000,
                    "currency": "inr",
                    "status": "requires_payment_method",
                    "client_secret": "pi_3MtwBwLkdIwHu7ix28a3tqPa_secret_xV7"
                }
                """;

        mockServer.expect(requestTo("https://api.stripe.com/v1/payment_intents"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .andRespond(withSuccess(stripeIntentJson, MediaType.APPLICATION_JSON));

        PaymentTransaction txn = PaymentTransaction.builder()
                .transactionReference("TXN_STRIPE_INIT_001")
                .amount(new BigDecimal("2500.00"))
                .currency("INR")
                .description("Test Order")
                .build();

        GatewayCredentials credentials = GatewayCredentials.builder().apiKey(apiKey).build();
        GatewayPaymentResult result = strategy.initiatePayment(txn, credentials);

        mockServer.verify();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getGatewayTransactionId()).isEqualTo("pi_3MtwBwLkdIwHu7ix28a3tqPa");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(result.getCheckoutPayload()).containsEntry("clientSecret", "pi_3MtwBwLkdIwHu7ix28a3tqPa_secret_xV7");
    }

    @Test
    @DisplayName("Should refund payment via RestClient POST")
    void shouldRefundPayment() {
        String apiKey = "sk_test_refund_key";
        String refundJson = """
                {
                    "id": "re_3MtwBwLkdIwHu7ix28a3tqPa",
                    "object": "refund",
                    "amount": 100000,
                    "status": "succeeded"
                }
                """;

        mockServer.expect(requestTo("https://api.stripe.com/v1/refunds"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey))
                .andRespond(withSuccess(refundJson, MediaType.APPLICATION_JSON));

        PaymentTransaction txn = PaymentTransaction.builder()
                .transactionReference("TXN_REFUND_001")
                .gatewayTransactionId("pi_3MtwBwLkdIwHu7ix28a3tqPa")
                .amount(new BigDecimal("1000.00"))
                .build();

        GatewayCredentials credentials = GatewayCredentials.builder().apiKey(apiKey).build();
        GatewayRefundResult result = strategy.refundPayment(txn, new BigDecimal("1000.00"), "Customer return", credentials);

        mockServer.verify();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getGatewayRefundId()).isEqualTo("re_3MtwBwLkdIwHu7ix28a3tqPa");
    }
}
