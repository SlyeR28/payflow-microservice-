package com.payflow.merchantservice.service;

import com.payflow.merchantservice.payload.requestDto.AddBankAccountRequest;
import com.payflow.merchantservice.payload.responseDto.BankAccountResponse;

import java.util.List;

public interface BankAccountService {

    BankAccountResponse addBankAccount(Long userId, AddBankAccountRequest request);

    List<BankAccountResponse> getBankAccounts(Long merchantId);

    BankAccountResponse getBankAccountById(Long accountId);

    BankAccountResponse setPrimaryBankAccount(Long merchantId, Long accountId);

    void deleteBankAccount(Long merchantId, Long accountId);
}
