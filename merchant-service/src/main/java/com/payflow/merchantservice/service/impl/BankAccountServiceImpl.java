package com.payflow.merchantservice.service.impl;

import com.payflow.merchantservice.exceptions.MerchantNotFoundException;
import com.payflow.merchantservice.mapper.BankAccountMapper;
import com.payflow.merchantservice.model.entity.BankAccount;
import com.payflow.merchantservice.model.entity.Merchant;
import com.payflow.merchantservice.payload.requestDto.AddBankAccountRequest;
import com.payflow.merchantservice.payload.responseDto.BankAccountResponse;
import com.payflow.merchantservice.repository.BankAccountRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import com.payflow.merchantservice.service.BankAccountService;
import com.payflow.merchantservice.utils.EncryptionUtil;
import com.payflow.merchantservice.utils.MaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountServiceImpl implements BankAccountService {

    private final MerchantRepository merchantRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankAccountMapper bankAccountMapper;
    private final EncryptionUtil encryptionUtil;

    private static final Integer MAX_ACCOUNT = 5;

    @Override
    public BankAccountResponse addBankAccount(Long userId, AddBankAccountRequest request) {
        // TODO: Implement logic (AES encrypt account number, extract last 4 digits, handle primary flag, penny drop mock/verify)
        Merchant merchant = merchantRepository.findByUserId(userId)
                .orElseThrow(() -> new MerchantNotFoundException(userId));

        // 1 Limit checking of user
        long count = bankAccountRepository.countByMerchantId(merchant.getId());
        

        return null;



//       return bankAccountMapper.toResponse(bankAccountRepository.save(bankAccount));

    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountResponse> getBankAccounts(Long merchantId) {
        // TODO: Implement logic (fetch bank accounts for merchant)
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

         return bankAccountRepository.
                findByMerchantId(merchant.getId())
                 .stream()
                 .map(bankAccountMapper::toResponse)
                 .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountResponse getBankAccountById(Long accountId) {
        // TODO: Implement logic (fetch bank account by id)
        return null;
    }

    @Override
    public BankAccountResponse setPrimaryBankAccount(Long merchantId, Long accountId) {
        // TODO: Implement logic (switch primary bank account)
        return null;
    }

    @Override
    public void deleteBankAccount(Long merchantId, Long accountId) {
        // TODO: Implement logic (prevent deleting primary account if other accounts exist, remove account)
    }
}
