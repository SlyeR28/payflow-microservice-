package com.payflow.ledgerservice.controller;

import com.payflow.common.dto.ApiResponse;
import com.payflow.ledgerservice.dto.request.CreateAccountRequest;
import com.payflow.ledgerservice.dto.request.RecordTransactionRequest;
import com.payflow.ledgerservice.dto.response.AccountResponse;
import com.payflow.ledgerservice.dto.response.JournalEntryResponse;
import com.payflow.ledgerservice.service.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<AccountResponse>>createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ){
        log.info("Received request to create ledger account for merchant: {}", request.getMerchantId());
        AccountResponse accountResponse = ledgerService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AccountResponse>builder()
                        .success(true)
                        .message("Ledger account created successfully")
                        .data(accountResponse)
                        .build());
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>>getAccount(
            @PathVariable String accountNumber
    ){
        AccountResponse account = ledgerService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.<AccountResponse>builder()
                        .success(true)
                        .message("Ledger account retrieved successfully")
                        .data(account)
                        .build());
    }


    @GetMapping("/accounts/merchant/{merchantId}")
    public ResponseEntity<ApiResponse<AccountResponse>>getAccountByMerchant(
            @PathVariable Long merchantId
    ){
        AccountResponse account = ledgerService.getAccountByMerchantId(merchantId);
        return ResponseEntity.ok(ApiResponse.<AccountResponse>builder()
                .success(true)
                .message("Merchant account retrieved successfully")
                .data(account)
                .build());
    }


    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<JournalEntryResponse>>createTransaction(
            @Valid @RequestBody RecordTransactionRequest request
    ){
        log.info("Recording financial transaction from source [{}] with idempotency key [{}]",
                request.getSourceSystem(), request.getIdempotencyKey());
        JournalEntryResponse entryResponse = ledgerService.recordTransactions(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<JournalEntryResponse>builder()
                        .success(true)
                        .message("Transaction recorded successfully")
                        .data(entryResponse)
                        .build());

    }

    @GetMapping("/transactions/{reference}")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> getTransaction(
            @PathVariable String reference) {
        JournalEntryResponse response = ledgerService.getTransactionsByReference(reference);
        return ResponseEntity.ok(ApiResponse.<JournalEntryResponse>builder()
                .success(true)
                .message("Transaction retrieved successfully")
                .data(response)
                .build());
    }



}
