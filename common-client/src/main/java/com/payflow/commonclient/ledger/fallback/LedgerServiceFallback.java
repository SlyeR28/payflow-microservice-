package com.payflow.commonclient.ledger.fallback;

import com.payflow.common.dto.ApiResponse;
import com.payflow.commonclient.ledger.LedgerServiceClient;
import com.payflow.commonclient.ledger.dto.AccountClientResponse;
import com.payflow.commonclient.ledger.dto.CreateAccountClientRequest;
import com.payflow.commonclient.ledger.dto.JournalClientResponse;
import com.payflow.commonclient.ledger.dto.LedgerTransactionClientRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LedgerServiceFallback implements LedgerServiceClient {


    @Override
    public ApiResponse<AccountClientResponse> createAccount(CreateAccountClientRequest request) {
        log.error("LedgerServiceFallback: Failed to provision ledger account for merchant: {}", request.getMerchantId());
        return ApiResponse.<AccountClientResponse>builder()
                .success(false)
                .message("Ledger service is currently unreachable. Account creation flagged for reconciliation.")
                .build();
    }

    @Override
    public ApiResponse<JournalClientResponse> recordTransaction(LedgerTransactionClientRequest request) {
        log.error("LedgerServiceClient fallback triggered for idempotencyKey: {}", request.getIdempotencyKey());
        return ApiResponse.<JournalClientResponse>builder()
                .success(false)
                .message("Ledger service is currently unreachable. Transaction flagged for reconciliation.")
                .build();
    }
    @Override
    public ApiResponse<AccountClientResponse> getAccountByNumber(String accountNumber) {
        return ApiResponse.<AccountClientResponse>builder().success(false).message("Ledger unreachable").build();
    }
    @Override
    public ApiResponse<AccountClientResponse> getAccountByMerchantId(Long merchantId) {
        return ApiResponse.<AccountClientResponse>builder().success(false).message("Ledger unreachable").build();
    }
}

