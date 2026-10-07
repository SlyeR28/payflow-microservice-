package com.payflow.paymentservice.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.paymentservice.dto.GatewayCredentials;
import com.payflow.paymentservice.dto.GatewayPaymentResult;
import com.payflow.paymentservice.dto.GatewayRefundResult;
import com.payflow.paymentservice.dto.GatewayVerificationResponse;
import com.payflow.paymentservice.exception.PaymentGatewayException;
import com.payflow.paymentservice.model.entity.PaymentTransaction;
import com.payflow.paymentservice.model.enums.PaymentProvider;
import com.payflow.paymentservice.model.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class StripePaymentStrategy implements PaymentGatewayStrategy {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public StripePaymentStrategy(
            RestClient.Builder restClientBuilder,
            @Value("${payment.stripe.base-url:https://api.stripe.com}") String stripeBaseUrl,
            ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.baseUrl(stripeBaseUrl).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public GatewayVerificationResponse verifyCredentials(GatewayCredentials credentials) {
        if (credentials == null || credentials.getApiKey() == null || credentials.getApiKey().isBlank()) {
            return GatewayVerificationResponse.failure(PaymentProvider.STRIPE, "Stripe secret API key is required");
        }

        String apiKey = credentials.getApiKey().trim();
        log.info("Performing live pre-flight verification against Stripe balance endpoint");

        try {
            ResponseEntity<String> response = restClient.get()
                    .uri("/v1/balance")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        String errMsg = extractStripeErrorMessage(body);
                        throw new PaymentGatewayException("Stripe authentication failed: " + errMsg);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new PaymentGatewayException("Stripe API returned 5xx server error during verification");
                    })
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                Map<String, Object> details = new HashMap<>();
                details.put("object", root.path("object").asText("balance"));
                details.put("livemode", root.path("livemode").asBoolean(false));
                details.put("status", "ACTIVE");
                details.put("provider", "STRIPE");

                return GatewayVerificationResponse.success(
                        PaymentProvider.STRIPE,
                        "Stripe sandbox API credentials verified successfully against live endpoint",
                        details
                );
            }

            return GatewayVerificationResponse.failure(PaymentProvider.STRIPE, "Unexpected response from Stripe");

        } catch (PaymentGatewayException ex) {
            log.warn("Stripe live credential verification failed: {}", ex.getMessage());
            return GatewayVerificationResponse.failure(PaymentProvider.STRIPE, ex.getMessage());
        } catch (ResourceAccessException ex) {
            log.error("Stripe live connection timeout or unreachable: {}", ex.getMessage());
            return GatewayVerificationResponse.failure(PaymentProvider.STRIPE, "Stripe API connection timed out or is unreachable");
        } catch (Exception ex) {
            log.error("Stripe verification unexpected error: {}", ex.getMessage());
            return GatewayVerificationResponse.failure(PaymentProvider.STRIPE, "Verification error: " + ex.getMessage());
        }
    }

    @Override
    public GatewayPaymentResult initiatePayment(PaymentTransaction transaction, GatewayCredentials credentials) {
        String apiKey = resolveApiKey(credentials);
        log.info("Initiating Stripe PaymentIntent for txn ref: {}", transaction.getTransactionReference());

        long amountInCents = transaction.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("amount", String.valueOf(amountInCents));
        form.add("currency", transaction.getCurrency().toLowerCase());
        form.add("description", transaction.getDescription() != null ? transaction.getDescription() : "PayFlow Txn " + transaction.getTransactionReference());
        form.add("metadata[payflow_ref]", transaction.getTransactionReference());
        form.add("automatic_payment_methods[enabled]", "true");

        try {
            ResponseEntity<String> response = restClient.post()
                    .uri("/v1/payment_intents")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        String errMsg = extractStripeErrorMessage(body);
                        throw new PaymentGatewayException("Stripe PaymentIntent creation failed: " + errMsg);
                    })
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String paymentIntentId = root.path("id").asText();
                String clientSecret = root.path("client_secret").asText();
                String stripeStatus = root.path("status").asText();

                Map<String, Object> checkoutPayload = new HashMap<>();
                checkoutPayload.put("paymentIntentId", paymentIntentId);
                checkoutPayload.put("clientSecret", clientSecret);
                checkoutPayload.put("stripeStatus", stripeStatus);

                return GatewayPaymentResult.success(paymentIntentId, PaymentStatus.AUTHORIZED, checkoutPayload);
            }

            return GatewayPaymentResult.failure("Failed to create Stripe PaymentIntent");

        } catch (Exception ex) {
            log.error("Stripe PaymentIntent error: {}", ex.getMessage());
            return GatewayPaymentResult.failure(ex.getMessage());
        }
    }

    @Override
    public GatewayRefundResult refundPayment(PaymentTransaction transaction, BigDecimal refundAmount, String reason, GatewayCredentials credentials) {
        String apiKey = resolveApiKey(credentials);
        log.info("Initiating Stripe Refund for gateway txn: {}", transaction.getGatewayTransactionId());

        long amountInCents = refundAmount.multiply(BigDecimal.valueOf(100)).longValue();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("payment_intent", transaction.getGatewayTransactionId());
        form.add("amount", String.valueOf(amountInCents));
        if (reason != null && !reason.isBlank()) {
            form.add("reason", "requested_by_customer");
        }

        try {
            ResponseEntity<String> response = restClient.post()
                    .uri("/v1/refunds")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        String errMsg = extractStripeErrorMessage(body);
                        throw new PaymentGatewayException("Stripe refund failed: " + errMsg);
                    })
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String refundId = root.path("id").asText();
                return GatewayRefundResult.success(refundId, refundAmount);
            }

            return GatewayRefundResult.failure("Failed to issue Stripe refund");

        } catch (Exception ex) {
            log.error("Stripe refund exception: {}", ex.getMessage());
            return GatewayRefundResult.failure(ex.getMessage());
        }
    }

    private String resolveApiKey(GatewayCredentials credentials) {
        if (credentials != null && credentials.getApiKey() != null && !credentials.getApiKey().isBlank()) {
            return credentials.getApiKey().trim();
        }
        throw new PaymentGatewayException("Stripe API key is required to execute transaction");
    }

    private String extractStripeErrorMessage(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.has("error") && root.get("error").has("message")) {
                return root.get("error").get("message").asText();
            }
        } catch (Exception ignored) {
        }
        return body;
    }
}
