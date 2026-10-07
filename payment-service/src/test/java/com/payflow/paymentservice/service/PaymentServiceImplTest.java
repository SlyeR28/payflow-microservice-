package com.payflow.paymentservice.service;

import com.payflow.paymentservice.dto.*;
import com.payflow.paymentservice.exception.PaymentGatewayException;
import com.payflow.paymentservice.exception.PaymentTransactionNotFoundException;
import com.payflow.paymentservice.model.entity.PaymentTransaction;
import com.payflow.paymentservice.model.enums.PaymentProvider;
import com.payflow.paymentservice.model.enums.PaymentStatus;
import com.payflow.paymentservice.repository.PaymentTransactionRepository;
import com.payflow.paymentservice.strategy.PaymentGatewayStrategy;
import com.payflow.paymentservice.strategy.PaymentStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentStrategyFactory strategyFactory;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private PaymentGatewayStrategy mockStrategy;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentTransaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = PaymentTransaction.builder()
                .id(1L)
                .transactionReference("TXN_UNIT_TEST_100")
                .provider(PaymentProvider.MOCK)
                .amount(new BigDecimal("1500.00"))
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .customerEmail("customer@test.com")
                .description("Unit Test Order")
                .build();
    }

    @Test
    @DisplayName("Should verify credentials via resolved strategy")
    void shouldVerifyCredentialsViaStrategy() {
        VerifyCredentialsRequest request = VerifyCredentialsRequest.builder()
                .provider(PaymentProvider.MOCK)
                .apiKey("test_mock_key")
                .build();

        GatewayVerificationResponse expectedResponse = GatewayVerificationResponse.success(
                PaymentProvider.MOCK, "Verified", Map.of("status", "ACTIVE")
        );

        when(strategyFactory.getStrategy(PaymentProvider.MOCK)).thenReturn(mockStrategy);
        when(mockStrategy.verifyCredentials(any())).thenReturn(expectedResponse);

        GatewayVerificationResponse actualResponse = paymentService.verifyCredentials(request);

        assertThat(actualResponse.isValid()).isTrue();
        assertThat(actualResponse.getProvider()).isEqualTo(PaymentProvider.MOCK);
        verify(mockStrategy).verifyCredentials(any());
    }

    @Test
    @DisplayName("Should initiate payment and transition status to AUTHORIZED")
    void shouldInitiatePaymentSuccessfully() {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .amount(new BigDecimal("1500.00"))
                .currency("INR")
                .provider(PaymentProvider.MOCK)
                .customerEmail("customer@test.com")
                .description("Unit Test Order")
                .build();

        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> {
            PaymentTransaction txn = invocation.getArgument(0);
            if (txn.getId() == null) {
                txn.setId(10L);
                txn.setTransactionReference("TXN_REF_10");
            }
            return txn;
        });

        when(strategyFactory.getStrategy(PaymentProvider.MOCK)).thenReturn(mockStrategy);
        when(mockStrategy.initiatePayment(any(PaymentTransaction.class), any()))
                .thenReturn(GatewayPaymentResult.success("mock_order_99", PaymentStatus.AUTHORIZED, Map.of("orderId", "mock_order_99")));

        PaymentInitiateResponse response = paymentService.initiatePayment(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(response.getGatewayTransactionId()).isEqualTo("mock_order_99");
        assertThat(response.getCheckoutPayload()).containsKey("orderId");
        verify(transactionRepository, times(2)).save(any(PaymentTransaction.class));
    }

    @Test
    @DisplayName("Should return existing transaction when idempotency key is already present")
    void shouldReturnExistingWhenIdempotencyKeyExists() {
        String idempotencyKey = "idem_key_unique_123";
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .amount(new BigDecimal("1500.00"))
                .currency("INR")
                .provider(PaymentProvider.MOCK)
                .idempotencyKey(idempotencyKey)
                .build();

        sampleTransaction.setIdempotencyKey(idempotencyKey);
        sampleTransaction.setStatus(PaymentStatus.AUTHORIZED);
        sampleTransaction.setGatewayTransactionId("mock_order_existing");

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(sampleTransaction));

        PaymentInitiateResponse response = paymentService.initiatePayment(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(response.getGatewayTransactionId()).isEqualTo("mock_order_existing");
        verify(mockStrategy, never()).initiatePayment(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should mark transaction as FAILED when strategy returns failure")
    void shouldMarkTransactionAsFailedOnGatewayError() {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .amount(new BigDecimal("1500.00"))
                .provider(PaymentProvider.MOCK)
                .build();

        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> {
            PaymentTransaction txn = invocation.getArgument(0);
            if (txn.getId() == null) {
                txn.setId(11L);
                txn.setTransactionReference("TXN_REF_11");
            }
            return txn;
        });

        when(strategyFactory.getStrategy(PaymentProvider.MOCK)).thenReturn(mockStrategy);
        when(mockStrategy.initiatePayment(any(PaymentTransaction.class), any()))
                .thenReturn(GatewayPaymentResult.failure("Insufficient funds on card"));

        PaymentInitiateResponse response = paymentService.initiatePayment(request);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.getFailureReason()).isEqualTo("Insufficient funds on card");
    }

    @Test
    @DisplayName("Should throw PaymentTransactionNotFoundException for unknown ID")
    void shouldThrowNotFoundForUnknownId() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getTransactionById(999L))
                .isInstanceOf(PaymentTransactionNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should process refund successfully")
    void shouldProcessRefundSuccessfully() {
        sampleTransaction.setStatus(PaymentStatus.AUTHORIZED);
        sampleTransaction.setGatewayTransactionId("mock_order_100");

        when(transactionRepository.findByTransactionReference("TXN_UNIT_TEST_100"))
                .thenReturn(Optional.of(sampleTransaction));
        when(strategyFactory.getStrategy(PaymentProvider.MOCK)).thenReturn(mockStrategy);
        when(mockStrategy.refundPayment(any(), eq(new BigDecimal("500.00")), any(), any()))
                .thenReturn(GatewayRefundResult.success("mock_rfnd_100", new BigDecimal("500.00")));

        RefundInitiateRequest request = RefundInitiateRequest.builder()
                .amount(new BigDecimal("500.00"))
                .reason("Defective item")
                .build();

        RefundResponse response = paymentService.refundPayment("TXN_UNIT_TEST_100", request);

        assertThat(response.getGatewayRefundId()).isEqualTo("mock_rfnd_100");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(sampleTransaction.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(transactionRepository).save(sampleTransaction);
    }

    @Test
    @DisplayName("Should throw exception when refunding already refunded transaction")
    void shouldThrowWhenRefundingAlreadyRefunded() {
        sampleTransaction.setStatus(PaymentStatus.REFUNDED);
        when(transactionRepository.findByTransactionReference("TXN_UNIT_TEST_100"))
                .thenReturn(Optional.of(sampleTransaction));

        assertThatThrownBy(() -> paymentService.refundPayment("TXN_UNIT_TEST_100", null))
                .isInstanceOf(PaymentGatewayException.class)
                .hasMessageContaining("already refunded");
    }

    @Test
    @DisplayName("Should throw exception when refund amount exceeds original amount")
    void shouldThrowWhenRefundAmountExceedsOriginal() {
        sampleTransaction.setStatus(PaymentStatus.AUTHORIZED);
        when(transactionRepository.findByTransactionReference("TXN_UNIT_TEST_100"))
                .thenReturn(Optional.of(sampleTransaction));

        RefundInitiateRequest request = RefundInitiateRequest.builder()
                .amount(new BigDecimal("2000.00")) // original is 1500.00
                .build();

        assertThatThrownBy(() -> paymentService.refundPayment("TXN_UNIT_TEST_100", request))
                .isInstanceOf(PaymentGatewayException.class)
                .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("Should return list of supported providers")
    void shouldReturnSupportedProviders() {
        when(strategyFactory.getSupportedProviders()).thenReturn(List.of(PaymentProvider.STRIPE, PaymentProvider.RAZORPAY, PaymentProvider.MOCK));
        List<PaymentProvider> providers = paymentService.getSupportedProviders();
        assertThat(providers).hasSize(3);
    }
}
