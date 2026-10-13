package com.payflow.commonclient.ledger;

import com.payflow.common.dto.ApiResponse;
import com.payflow.commonclient.ledger.dto.AccountClientResponse;
import com.payflow.commonclient.ledger.dto.CreateAccountClientRequest;
import com.payflow.commonclient.ledger.dto.JournalClientResponse;
import com.payflow.commonclient.ledger.dto.LedgerTransactionClientRequest;
import com.payflow.commonclient.ledger.fallback.LedgerServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ledger-service" , fallback = LedgerServiceFallback.class)
public interface LedgerServiceClient {


    @PostMapping("/api/v1/ledger/accounts")
    ApiResponse<AccountClientResponse> createAccount(@RequestBody CreateAccountClientRequest request);

    @PostMapping("/api/v1/ledger/transactions")
    ApiResponse<JournalClientResponse> recordTransaction(@RequestBody LedgerTransactionClientRequest request);

    @GetMapping("/api/v1/ledger/accounts/{accountNumber}")
    ApiResponse<AccountClientResponse> getAccountByNumber(@PathVariable("accountNumber") String accountNumber);

    @GetMapping("/api/v1/ledger/accounts/merchant/{merchantId}")
    ApiResponse<AccountClientResponse> getAccountByMerchantId(@PathVariable("merchantId") Long merchantId);
}
