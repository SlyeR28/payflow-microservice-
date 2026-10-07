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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentStrategyFactory strategyFactory;
    private final PaymentTransactionRepository transactionRepository;

    @Override
    public GatewayVerificationResponse verifyCredentials(VerifyCredentialsRequest request) {
        log.info("Verifying credentials for provider: {}", request.getProvider());
        PaymentGatewayStrategy strategy = strategyFactory.getStrategy(request.getProvider());

        GatewayCredentials credentials = GatewayCredentials.builder()
                .apiKey(request.getApiKey())
                .apiSecret(request.getApiSecret())
                .webhookSecret(request.getWebhookSecret())
                .build();

        return strategy.verifyCredentials(credentials);
    }

    @Override
    @Transactional
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {
        // 1. Idempotency Check
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<PaymentTransaction> existingTxn = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingTxn.isPresent()) {
                log.info("Returning existing transaction for idempotency key: {}", request.getIdempotencyKey());
                return mapToResponse(existingTxn.get(), null);
            }
        }

        // 2. Persist Initial Pending Transaction
        PaymentTransaction transaction = PaymentTransaction.builder()
                .provider(request.getProvider())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency().toUpperCase() : "INR")
                .status(PaymentStatus.PENDING)
                .customerEmail(request.getCustomerEmail())
                .description(request.getDescription())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Created pending transaction: {} with ID: {}", transaction.getTransactionReference(), transaction.getId());

        // 3. Resolve Strategy & Invoke Gateway
        PaymentGatewayStrategy strategy = strategyFactory.getStrategy(request.getProvider());
        GatewayPaymentResult result = strategy.initiatePayment(transaction, request.getCredentials());

        // 4. Update Transaction with Gateway Result
        if (result.isSuccess()) {
            transaction.setStatus(result.getStatus() != null ? result.getStatus() : PaymentStatus.AUTHORIZED);
            transaction.setGatewayTransactionId(result.getGatewayTransactionId());
            transaction = transactionRepository.save(transaction);
            log.info("Payment initiated successfully: ref={}, gatewayTxnId={}",
                    transaction.getTransactionReference(), transaction.getGatewayTransactionId());
            return mapToResponse(transaction, result.getCheckoutPayload());
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setFailureReason(result.getErrorMessage());
            transaction = transactionRepository.save(transaction);
            log.warn("Payment initiation failed for ref {}: {}", transaction.getTransactionReference(), result.getErrorMessage());
            return mapToResponse(transaction, null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentInitiateResponse getTransactionById(Long id) {
        PaymentTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new PaymentTransactionNotFoundException(id));
        return mapToResponse(transaction, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentInitiateResponse getTransactionByReference(String reference) {
        PaymentTransaction transaction = transactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new PaymentTransactionNotFoundException(reference));
        return mapToResponse(transaction, null);
    }

    @Override
    @Transactional
    public RefundResponse refundPayment(String reference, RefundInitiateRequest request) {
        PaymentTransaction transaction = transactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new PaymentTransactionNotFoundException(reference));

        if (transaction.getStatus() == PaymentStatus.REFUNDED) {
            throw new PaymentGatewayException("Transaction is already refunded: " + reference);
        }
        if (transaction.getStatus() == PaymentStatus.FAILED || transaction.getStatus() == PaymentStatus.PENDING) {
            throw new PaymentGatewayException("Cannot refund transaction in status: " + transaction.getStatus());
        }

        BigDecimal refundAmount = (request != null && request.getAmount() != null)
                ? request.getAmount()
                : transaction.getAmount();

        if (refundAmount.compareTo(transaction.getAmount()) > 0) {
            throw new PaymentGatewayException("Refund amount cannot exceed original transaction amount of " + transaction.getAmount());
        }

        PaymentGatewayStrategy strategy = strategyFactory.getStrategy(transaction.getProvider());
        GatewayCredentials credentials = request != null ? request.getCredentials() : null;
        String reason = request != null ? request.getReason() : "Customer refund request";

        GatewayRefundResult refundResult = strategy.refundPayment(transaction, refundAmount, reason, credentials);

        if (!refundResult.isSuccess()) {
            throw new PaymentGatewayException("Refund failed: " + refundResult.getErrorMessage());
        }

        transaction.setStatus(PaymentStatus.REFUNDED);
        transactionRepository.save(transaction);
        log.info("Transaction {} refunded successfully with refund ID: {}", reference, refundResult.getGatewayRefundId());

        return RefundResponse.builder()
                .transactionReference(reference)
                .gatewayRefundId(refundResult.getGatewayRefundId())
                .refundedAmount(refundAmount)
                .status(PaymentStatus.REFUNDED)
                .message("Refund processed successfully")
                .refundedAt(Instant.now())
                .build();
    }

    @Override
    public List<PaymentProvider> getSupportedProviders() {
        return strategyFactory.getSupportedProviders();
    }

    private PaymentInitiateResponse mapToResponse(PaymentTransaction txn, java.util.Map<String, Object> payload) {
        return PaymentInitiateResponse.builder()
                .id(txn.getId())
                .transactionReference(txn.getTransactionReference())
                .provider(txn.getProvider())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .status(txn.getStatus())
                .gatewayTransactionId(txn.getGatewayTransactionId())
                .customerEmail(txn.getCustomerEmail())
                .description(txn.getDescription())
                .idempotencyKey(txn.getIdempotencyKey())
                .failureReason(txn.getFailureReason())
                .checkoutPayload(payload)
                .createdAt(txn.getCreatedAt())
                .updatedAt(txn.getUpdatedAt())
                .build();
    }
}
