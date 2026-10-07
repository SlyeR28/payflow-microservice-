package com.payflow.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.payflow.paymentservice.dto.*;
import com.payflow.paymentservice.exception.PaymentGlobalExceptionHandler;
import com.payflow.paymentservice.model.enums.PaymentProvider;
import com.payflow.paymentservice.model.enums.PaymentStatus;
import com.payflow.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        PaymentController controller = new PaymentController(paymentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new PaymentGlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /verify-credentials - Success")
    void testVerifyCredentialsSuccess() throws Exception {
        VerifyCredentialsRequest request = VerifyCredentialsRequest.builder()
                .provider(PaymentProvider.STRIPE)
                .apiKey("sk_test_mockApiKey123")
                .build();

        GatewayVerificationResponse response = GatewayVerificationResponse.success(
                PaymentProvider.STRIPE, "Verified", Map.of("status", "ACTIVE")
        );

        when(paymentService.verifyCredentials(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/verify-credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.provider").value("STRIPE"));
    }

    @Test
    @DisplayName("POST /verify-credentials - Validation Failure (Missing apiKey)")
    void testVerifyCredentialsValidationFailure() throws Exception {
        VerifyCredentialsRequest request = VerifyCredentialsRequest.builder()
                .provider(PaymentProvider.STRIPE)
                .apiKey("") // blank
                .build();

        mockMvc.perform(post("/api/v1/payments/verify-credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /initiate - Success (201 Created)")
    void testInitiatePaymentSuccess() throws Exception {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .amount(new BigDecimal("750.00"))
                .currency("INR")
                .provider(PaymentProvider.MOCK)
                .customerEmail("user@example.com")
                .build();

        PaymentInitiateResponse response = PaymentInitiateResponse.builder()
                .id(1L)
                .transactionReference("TXN_12345")
                .provider(PaymentProvider.MOCK)
                .amount(new BigDecimal("750.00"))
                .currency("INR")
                .status(PaymentStatus.AUTHORIZED)
                .gatewayTransactionId("mock_order_12345")
                .checkoutPayload(Map.of("orderId", "mock_order_12345"))
                .createdAt(Instant.now())
                .build();

        when(paymentService.initiatePayment(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.transactionReference").value("TXN_12345"))
                .andExpect(jsonPath("$.data.status").value("AUTHORIZED"));
    }

    @Test
    @DisplayName("POST /initiate - Validation Failure (Zero or negative amount)")
    void testInitiatePaymentInvalidAmount() throws Exception {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .amount(new BigDecimal("-10.00"))
                .provider(PaymentProvider.MOCK)
                .build();

        mockMvc.perform(post("/api/v1/payments/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /{id} - Returns Transaction")
    void testGetTransactionById() throws Exception {
        PaymentInitiateResponse response = PaymentInitiateResponse.builder()
                .id(5L)
                .transactionReference("TXN_555")
                .provider(PaymentProvider.RAZORPAY)
                .amount(new BigDecimal("1200.00"))
                .status(PaymentStatus.AUTHORIZED)
                .build();

        when(paymentService.getTransactionById(5L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.transactionReference").value("TXN_555"));
    }

    @Test
    @DisplayName("GET /ref/{reference} - Returns Transaction")
    void testGetTransactionByReference() throws Exception {
        PaymentInitiateResponse response = PaymentInitiateResponse.builder()
                .id(6L)
                .transactionReference("TXN_REF_666")
                .provider(PaymentProvider.STRIPE)
                .amount(new BigDecimal("3000.00"))
                .status(PaymentStatus.AUTHORIZED)
                .build();

        when(paymentService.getTransactionByReference("TXN_REF_666")).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/ref/TXN_REF_666"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionReference").value("TXN_REF_666"));
    }

    @Test
    @DisplayName("POST /{reference}/refund - Success")
    void testRefundPayment() throws Exception {
        RefundInitiateRequest request = RefundInitiateRequest.builder()
                .amount(new BigDecimal("500.00"))
                .reason("Defective product")
                .build();

        RefundResponse response = RefundResponse.builder()
                .transactionReference("TXN_REF_666")
                .gatewayRefundId("re_test_999")
                .refundedAmount(new BigDecimal("500.00"))
                .status(PaymentStatus.REFUNDED)
                .message("Refund processed successfully")
                .refundedAt(Instant.now())
                .build();

        when(paymentService.refundPayment(eq("TXN_REF_666"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/TXN_REF_666/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.gatewayRefundId").value("re_test_999"))
                .andExpect(jsonPath("$.data.status").value("REFUNDED"));
    }

    @Test
    @DisplayName("GET /providers - Returns Provider List")
    void testGetProviders() throws Exception {
        when(paymentService.getSupportedProviders()).thenReturn(List.of(
                PaymentProvider.STRIPE, PaymentProvider.RAZORPAY, PaymentProvider.MOCK
        ));

        mockMvc.perform(get("/api/v1/payments/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0]").value("STRIPE"))
                .andExpect(jsonPath("$.data[1]").value("RAZORPAY"))
                .andExpect(jsonPath("$.data[2]").value("MOCK"));
    }
}
