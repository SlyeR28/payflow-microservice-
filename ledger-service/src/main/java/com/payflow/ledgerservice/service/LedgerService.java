package com.payflow.ledgerservice.service;

import com.payflow.ledgerservice.dto.request.CreateAccountRequest;
import com.payflow.ledgerservice.dto.request.RecordTransactionRequest;
import com.payflow.ledgerservice.dto.response.AccountResponse;
import com.payflow.ledgerservice.dto.response.JournalEntryResponse;

public interface LedgerService {

    /**
     * Creates a new Financial ledger Account and initializes its snapshot balance to 0.000
     */
    AccountResponse createAccount(CreateAccountRequest request);

     /**
      * Atomically records a double entry financial transaction.
      * Enforces idempotency , double-entry balance check (sum Debits == sum Credits)
      * and updates real-time account balance with optimistic locking
      */
    JournalEntryResponse recordTransactions(RecordTransactionRequest request);

    /**
     *  Retrieves an account and its real-time balances by public account number.
     */
    AccountResponse getAccountByNumber(String accountNumber);


    /**
     * Retrieves an account and its real-time balances by merchant ID.
     */
    AccountResponse getAccountByMerchantId(Long merchantId);


    /**
     * Retrieves an immutable transaction receipt by entry reference.
     */
    JournalEntryResponse getTransactionsByReference(String reference);

}
