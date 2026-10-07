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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RazorpayPaymentStrategy implements PaymentGatewayStrategy {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public RazorpayPaymentStrategy(
            RestClient.Builder restClientBuilder,
            @Value("${payment.razorpay.base-url:https://api.razorpay.com}") String razorpayBaseUrl,
            ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.baseUrl(razorpayBaseUrl).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.RAZORPAY;
    }

    @Override
    public GatewayVerificationResponse verifyCredentials(GatewayCredentials credentials) {
        if (credentials == null || credentials.getApiKey() == null || credentials.getApiKey().isBlank()) {
            return GatewayVerificationResponse.failure(PaymentProvider.RAZORPAY, "Razorpay Key ID is required");
        }
        if (credentials.getApiSecret() == null || credentials.getApiSecret().isBlank()) {
            return GatewayVerificationResponse.failure(PaymentProvider.RAZORPAY, "Razorpay Key Secret is required");
        }

        String authHeader = buildBasicAuth(credentials.getApiKey().trim(), credentials.getApiSecret().trim());
        log.info("Performing live pre-flight verification against Razorpay payments endpoint");

        try {
            ResponseEntity<String> response = restClient.get()
                    .uri("/v1/payments?count=1")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        String errMsg = extractRazorpayErrorMessage(body);
                        throw new PaymentGatewayException("Razorpay authentication failed: " + errMsg);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new PaymentGatewayException("Razorpay API returned 5xx server error during verification");
                    })
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                Map<String, Object> details = new HashMap<>();
                details.put("entity", root.path("entity").asText("collection"));
                details.put("status", "ACTIVE");
                details.put("keyId", credentials.getApiKey());
                details.put("provider", "RAZORPAY");

                return GatewayVerificationResponse.success(
                        PaymentProvider.RAZORPAY,
                        "Razorpay sandbox credentials verified successfully against live API",
                        details
                );
            }

            return GatewayVerificationResponse.failure(PaymentProvider.RAZORPAY, "Unexpected response from Razorpay");

        } catch (PaymentGatewayException ex) {
            log.warn("Razorpay live verification failed: {}", ex.getMessage());
            return GatewayVerificationResponse.failure(PaymentProvider.RAZORPAY, ex.getMessage());
        } catch (ResourceAccessException ex) {
            log.error("Razorpay live connection timeout or unreachable: {}", ex.getMessage());
            return GatewayVerificationResponse.failure(PaymentProvider.RAZORPAY, "Razorpay API connection timed out or is unreachable");
        } catch (Exception ex) {
            log.error("Razorpay verification unexpected error: {}", ex.getMessage());
            return GatewayVerificationResponse.failure(PaymentProvider.RAZORPAY, "Verification error: " + ex.getMessage());
        }
    }

    @Override
    public GatewayPaymentResult initiatePayment(PaymentTransaction transaction, GatewayCredentials credentials) {
        validateCredentials(credentials);
        String authHeader = buildBasicAuth(credentials.getApiKey().trim(), credentials.getApiSecret().trim());
        log.info("Initiating Razorpay Order for txn ref: {}", transaction.getTransactionReference());

        long amountInPaise = transaction.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", amountInPaise);
        requestBody.put("currency", transaction.getCurrency().toUpperCase());
        requestBody.put("receipt", transaction.getTransactionReference());

        Map<String, String> notes = new HashMap<>();
        notes.put("payflow_reference", transaction.getTransactionReference());
        if (transaction.getCustomerEmail() != null) {
            notes.put("customer_email", transaction.getCustomerEmail());
        }
        requestBody.put("notes", notes);

        try {
            ResponseEntity<String> response = restClient.post()
                    .uri("/v1/orders")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        String errMsg = extractRazorpayErrorMessage(body);
                        throw new PaymentGatewayException("Razorpay Order creation failed: " + errMsg);
                    })
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String orderId = root.path("id").asText();
                String orderStatus = root.path("status").asText();

                Map<String, Object> checkoutPayload = new HashMap<>();
                checkoutPayload.put("orderId", orderId);
                checkoutPayload.put("keyId", credentials.getApiKey());
                checkoutPayload.put("amount", amountInPaise);
                checkoutPayload.put("currency", transaction.getCurrency());
                checkoutPayload.put("razorpayStatus", orderStatus);

                return GatewayPaymentResult.success(orderId, PaymentStatus.AUTHORIZED, checkoutPayload);
            }

            return GatewayPaymentResult.failure("Failed to create Razorpay Order");

        } catch (Exception ex) {
            log.error("Razorpay order creation error: {}", ex.getMessage());
            return GatewayPaymentResult.failure(ex.getMessage());
        }
    }

    @Override
    public GatewayRefundResult refundPayment(PaymentTransaction transaction, BigDecimal refundAmount, String reason, GatewayCredentials credentials) {
        validateCredentials(credentials);
        String authHeader = buildBasicAuth(credentials.getApiKey().trim(), credentials.getApiSecret().trim());
        log.info("Initiating Razorpay Refund for gateway payment: {}", transaction.getGatewayTransactionId());

        long amountInPaise = refundAmount.multiply(BigDecimal.valueOf(100)).longValue();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", amountInPaise);
        if (reason != null && !reason.isBlank()) {
            Map<String, String> notes = new HashMap<>();
            notes.put("reason", reason);
            requestBody.put("notes", notes);
        }

        try {
            ResponseEntity<String> response = restClient.post()
                    .uri("/v1/payments/{payment_id}/refund", transaction.getGatewayTransactionId())
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        String errMsg = extractRazorpayErrorMessage(body);
                        throw new PaymentGatewayException("Razorpay refund failed: " + errMsg);
                    })
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String refundId = root.path("id").asText();
                return GatewayRefundResult.success(refundId, refundAmount);
            }

            return GatewayRefundResult.failure("Failed to issue Razorpay refund");

        } catch (Exception ex) {
            log.error("Razorpay refund exception: {}", ex.getMessage());
            return GatewayRefundResult.failure(ex.getMessage());
        }
    }

    private void validateCredentials(GatewayCredentials credentials) {
        if (credentials == null || credentials.getApiKey() == null || credentials.getApiKey().isBlank()
                || credentials.getApiSecret() == null || credentials.getApiSecret().isBlank()) {
            throw new PaymentGatewayException("Razorpay Key ID and Secret are both required");
        }
    }

    private String buildBasicAuth(String keyId, String secret) {
        String token = keyId + ":" + secret;
        return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private String extractRazorpayErrorMessage(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.has("error") && root.get("error").has("description")) {
                return root.get("error").get("description").asText();
            }
        } catch (Exception ignored) {
        }
        return body;
    }
}
