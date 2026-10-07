package com.payflow.merchantservice.controller;

import com.payflow.common.dto.ApiResponse;
import com.payflow.merchantservice.payload.requestDto.AddBankAccountRequest;
import com.payflow.merchantservice.payload.responseDto.BankAccountResponse;
import com.payflow.merchantservice.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<ApiResponse<BankAccountResponse>> addBankAccount(
            @PathVariable Long merchantId,
            @Valid @RequestBody AddBankAccountRequest request) {
        BankAccountResponse response = bankAccountService.addBankAccount(merchantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<BankAccountResponse>builder()
                        .success(true)
                        .message("Bank account added successfully")
                        .data(response)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> getBankAccounts(@PathVariable Long merchantId) {
        List<BankAccountResponse> response = bankAccountService.getBankAccounts(merchantId);
        return ResponseEntity.ok(ApiResponse.<List<BankAccountResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> getBankAccountById(
            @PathVariable Long merchantId,
            @PathVariable Long accountId) {
        BankAccountResponse response = bankAccountService.getBankAccountById(accountId);
        return ResponseEntity.ok(ApiResponse.<BankAccountResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PatchMapping("/{accountId}/primary")
    public ResponseEntity<ApiResponse<BankAccountResponse>> setPrimaryBankAccount(
            @PathVariable Long merchantId,
            @PathVariable Long accountId) {
        BankAccountResponse response = bankAccountService.setPrimaryBankAccount(merchantId, accountId);
        return ResponseEntity.ok(ApiResponse.<BankAccountResponse>builder()
                .success(true)
                .message("Primary bank account updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse<Void>> deleteBankAccount(
            @PathVariable Long merchantId,
            @PathVariable Long accountId) {
        bankAccountService.deleteBankAccount(merchantId, accountId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Bank account removed successfully")
                .build());
    }
}
