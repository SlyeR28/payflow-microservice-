package com.payflow.paymentservice.service;

import com.payflow.common.dto.ApiResponse;
import com.payflow.commonclient.ledger.LedgerServiceClient;
import com.payflow.commonclient.ledger.dto.JournalClientResponse;
import com.payflow.commonclient.ledger.dto.LedgerPostingClientDto;
import com.payflow.commonclient.ledger.dto.LedgerTransactionClientRequest;
import com.payflow.commonclient.merchant.MerchantServiceClient;
import com.payflow.commonclient.merchant.dto.MerchantClientResponse;
import com.payflow.commonclient.notification.NotificationServiceClient;
import com.payflow.commonclient.notification.dto.NotificationClientRequest;
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
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentStrategyFactory strategyFactory;
    private final PaymentTransactionRepository transactionRepository;
    private final MerchantServiceClient merchantServiceClient;
    private final LedgerServiceClient ledgerServiceClient;
    private final NotificationServiceClient notificationServiceClient;

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
        // pre flight merchant verification Fail-Fast

        MerchantClientResponse merchant = null;

        if (request.getMerchantId() != null){
            try{
                ApiResponse<MerchantClientResponse> merchantById = merchantServiceClient
                        .getMerchantById(request.getMerchantId());
                if (merchantById == null || !merchantById.isSuccess() || merchantById.getData() == null){
                    throw new PaymentGatewayException("Merchant pre flight verification failed: Merchant Not Found " +
                            "for Id: " + request.getMerchantId());
                }
                merchant = merchantById.getData();
                if (!"ACTIVE".equalsIgnoreCase(merchant.getStatus())){
                    throw new PaymentGatewayException("Merchant pre flight verification failed: Merchant Not Active " +
                            "for Id: " + request.getMerchantId());
                }
                log.info("Merchant {} verified active. Escrow account: {}", request.getMerchantId()
                        , merchant.getEscrowAccountNumber());
            } catch (PaymentGatewayException e) {
                throw e;
            } catch (Exception e) {
                throw new PaymentGatewayException("Merchant pre flight verification failed: " + e.getMessage());
            }
        }


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

            // Double Entry Financial Capture via ledger Service
              recordLedgerCapture(transaction , merchant);
              sendPaymentNotification(transaction, merchant, "PAYMENT_SUCCESS", "Payment Successful - "
                      + transaction.getTransactionReference());

            return mapToResponse(transaction, result.getCheckoutPayload());
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setFailureReason(result.getErrorMessage());
            transaction = transactionRepository.save(transaction);

            log.warn("Payment initiation failed for ref {}: {}", transaction.getTransactionReference(), result.getErrorMessage());
            sendPaymentNotification(transaction, merchant, "PAYMENT_FAILED",
                    "Payment Failed - " + transaction.getTransactionReference());
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

        // Reversing Ledger Entry & Notification
          recordLedgerRefund(transaction , refundAmount, refundResult.getGatewayRefundId());
          sendRefundNotification(transaction, refundAmount, refundResult.getGatewayRefundId());


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


    // private Helper methods that solve the
    private void recordLedgerCapture(PaymentTransaction transaction , MerchantClientResponse merchant){

        try{
         String merchantEscrowAccountNumber =  (merchant != null && merchant.getEscrowAccountNumber() != null) ?
                    merchant.getEscrowAccountNumber()
                    : "ACC-M" + transaction.getMerchantId() + "-" + transaction.getCurrency();


            LedgerPostingClientDto debitClearing  = LedgerPostingClientDto.builder()
                    .accountNumber("ACC-SYS-CLEARING-" + transaction.getCurrency())
                    .direction("DEBIT")
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .build();


            LedgerPostingClientDto creditMerchant  = LedgerPostingClientDto.builder()
                    .accountNumber(merchantEscrowAccountNumber)
                    .direction("CREDIT")
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .build();

            LedgerTransactionClientRequest transactionClientRequest = LedgerTransactionClientRequest.builder()
                    .sourceSystem("PAYMENT GATEWAY")
                    .entryType("PAYMENT_CAPTURE")
                    .description("`Payment capture for ref: " + transaction.getTransactionReference())
                    .idempotencyKey("LEDGER_CAP_" + transaction.getTransactionReference())
                    .postings(List.of(debitClearing, creditMerchant))
                    .build();

            ApiResponse<JournalClientResponse> ledgerRes = ledgerServiceClient
                    .recordTransaction(transactionClientRequest);

            log.info("Ledger Capture posted for {}: success={}", transaction.getTransactionReference(), ledgerRes != null && ledgerRes.isSuccess());

        }catch (Exception e){
            log.error("Failed to record ledger capture for transaction {}", transaction.getTransactionReference(), e);
        }
    }

    private void recordLedgerRefund(PaymentTransaction transaction , BigDecimal refundAmount , String gatewayRefundId){
        try{
            String merchantEscrowAccount = "ACC-M-" + transaction.getMerchantId() + "-" +transaction.getCurrency();

            if (transaction.getMerchantId() != null){
                try{
                    ApiResponse<MerchantClientResponse> mRes = merchantServiceClient
                            .getMerchantById(transaction.getMerchantId());
                    if (mRes != null && mRes.isSuccess() && mRes.getData() != null && mRes.getData().getEscrowAccountNumber() != null) {
                        merchantEscrowAccount = mRes.getData().getEscrowAccountNumber();
                    }
                } catch (Exception e) {
                    log.warn("Using default merchant account number for merchant id {}", transaction.getMerchantId(), e);
                }
            }

            LedgerPostingClientDto debitMerchant = LedgerPostingClientDto.builder()
                    .accountNumber(merchantEscrowAccount)
                    .direction("DEBIT")
                    .amount(refundAmount)
                    .currency(transaction.getCurrency())
                    .build();

            LedgerPostingClientDto creditClearing = LedgerPostingClientDto.builder()
                    .accountNumber("ACC-SYS-CLEARING" + transaction.getCurrency())
                    .direction("CREDIT")
                    .amount(refundAmount)
                    .currency(transaction.getCurrency())
                    .build();

            LedgerTransactionClientRequest clientRequest = LedgerTransactionClientRequest.builder()
                    .sourceSystem("PAYMENT_GATEWAY")
                    .entryType("PAYMENT_REFUND")
                    .description("`Payment refund for ref: " + transaction.getTransactionReference())
                    .idempotencyKey("LEDGER_REFUND_" + transaction.getTransactionReference() + "-" + gatewayRefundId)
                    .postings(List.of(debitMerchant, creditClearing))
                    .build();


            ledgerServiceClient.recordTransaction(clientRequest);
            log.info("ledger refund Journal posted for {}", transaction.getTransactionReference());

        } catch (Exception e) {
            log.error("Failed to post ledger refund for {} : {}",transaction.getTransactionReference() , e.getMessage());
        }
    }


    private void sendPaymentNotification(PaymentTransaction txn , MerchantClientResponse response
            , String templateType , String subject){

        try {
            String recipient = (txn.getCustomerEmail() != null && !txn.getCustomerEmail().isBlank()) ?
                    txn.getCustomerEmail()
                    : (response != null ? response.getBusinessEmail() : null);

            if (recipient == null || recipient.isBlank())  {
                log.warn("No recipient found for transaction {}", txn.getTransactionReference());
                return;
            }

            NotificationClientRequest notificationClientRequest = NotificationClientRequest.builder()
                    .idempotencyKey("NOTIF_" + txn.getTransactionReference() + "-" + templateType)
                    .merchantId(txn.getMerchantId())
                    .recipient(recipient)
                    .channel("EMAIL")
                    .templateType(templateType)
                    .priority("HIGH")
                    .subject(subject)
                    .payload(Map.of(
                            "transactionReference", txn.getTransactionReference(),
                            "amount", txn.getAmount(),
                            "currency", txn.getCurrency(),
                            "status", txn.getStatus().name()
                    ))
                    .build();

            notificationServiceClient.sendNotification(notificationClientRequest);
            log.info("Dispatched {} notification for {}", templateType, txn.getTransactionReference());
        } catch (Exception e) {
            log.warn("Non-blocking notification dispatch failed for {}: {}", txn.getTransactionReference(), e.getMessage());
        }
    }

     private void sendRefundNotification(PaymentTransaction txn, BigDecimal refundAmount, String gatewayRefundId) {
        try {
            if (txn.getCustomerEmail() == null || txn.getCustomerEmail().isBlank()) {
                return;
            }
            NotificationClientRequest request = NotificationClientRequest.builder()
                    .idempotencyKey("NOTIF_REFUND_" + txn.getTransactionReference())
                    .merchantId(txn.getMerchantId())
                    .recipient(txn.getCustomerEmail())
                    .channel("EMAIL")
                    .templateType("REFUND_PROCESSED")
                    .priority("HIGH")
                    .subject("Refund Processed - " + txn.getTransactionReference())
                    .payload(Map.of(
                            "transactionReference", txn.getTransactionReference(),
                            "refundedAmount", refundAmount,
                            "gatewayRefundId", gatewayRefundId != null ? gatewayRefundId : ""
                    ))
                    .build();
            notificationServiceClient.sendNotification(request);
        } catch (Exception e) {
            log.warn("Refund notification dispatch failed: {}", e.getMessage());
        }
    }
}
