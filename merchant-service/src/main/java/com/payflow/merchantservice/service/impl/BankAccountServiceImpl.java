package com.payflow.merchantservice.service.impl;

import com.payflow.common.exceptions.BusinessException;
import com.payflow.merchantservice.exceptions.BankAccountNotFoundException;
import com.payflow.merchantservice.exceptions.MerchantNotFoundException;
import com.payflow.merchantservice.exceptions.PrimaryBankAccountCannotBeDeletedException;
import com.payflow.merchantservice.mapper.BankAccountMapper;
import com.payflow.merchantservice.model.entity.BankAccount;
import com.payflow.merchantservice.model.entity.Merchant;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import com.payflow.merchantservice.payload.requestDto.AddBankAccountRequest;
import com.payflow.merchantservice.payload.responseDto.BankAccountResponse;
import com.payflow.merchantservice.repository.BankAccountRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import com.payflow.common.exceptions.DuplicateResourceException;
import com.payflow.merchantservice.exceptions.InvalidMerchantStateException;
import com.payflow.merchantservice.service.BankAccountService;
import com.payflow.merchantservice.utils.BankAccountHashUtility;
import com.payflow.merchantservice.utils.EncryptionUtil;
import com.payflow.merchantservice.utils.MaskingUtil;
import com.payflow.merchantservice.service.verification.dto.VerificationResult;
import com.payflow.merchantservice.service.verification.impl.VerificationEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountServiceImpl implements BankAccountService {

    private final MerchantRepository merchantRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankAccountMapper bankAccountMapper;
    private final EncryptionUtil encryptionUtil;
    private final VerificationEngineService verificationEngineService;

    private static final Integer MAX_ACCOUNT = 5;

    @Override
    public BankAccountResponse addBankAccount(Long merchantId, AddBankAccountRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        if (merchant.getStatus() == MerchantStatus.SUSPENDED 
                || merchant.getStatus() == MerchantStatus.REJECTED) {
            throw new InvalidMerchantStateException("Merchant must be active to add a bank account. Current status: " + merchant.getStatus());
        }

        // 1. Quota check
        long count = bankAccountRepository.countByMerchantId(merchantId);
        if (count >= MAX_ACCOUNT) {
            throw new BusinessException("Maximum " + MAX_ACCOUNT + " bank accounts allowed",
                    "BANK_ACCOUNT_LIMIT");
        }

        // 2. Compute deterministic SHA-256 hash for duplicate check
        String accountHash = BankAccountHashUtility.computeHash(request.getAccountNumber());

        // 3. Duplicate check for this merchant
        if (bankAccountRepository.findByMerchantIdAndAccountNumberHash(merchantId, accountHash).isPresent()) {
            throw new DuplicateResourceException("This bank account is already registered on your profile.");
        }

        // 4. Syndicate cross-merchant AML fraud alert
        long globalCount = bankAccountRepository.countByAccountNumberHash(accountHash);
        if (globalCount >= 2) {
            log.warn("🚨 [AML-ALERT] Bank account hash {} is linked to {} different merchant accounts! MerchantId={}",
                    accountHash, globalCount, merchantId);
        }

        // 5. Encrypt sensitive account number with AES-256-GCM and extract last 4
        String encryptedAccountNumber = encryptionUtil.encrypt(request.getAccountNumber());
        String normalizedAccount = BankAccountHashUtility.normalize(request.getAccountNumber());
        String last4 = normalizedAccount.length() >= 4 
                ? normalizedAccount.substring(normalizedAccount.length() - 4) 
                : normalizedAccount;

        // 6. Primary account toggle: If requested or if this is the first account
        boolean makePrimary = Boolean.TRUE.equals(request.getIsPrimary()) || count == 0;
        if (makePrimary) {
            bankAccountRepository.findByMerchantIdAndIsPrimaryTrue(merchantId).ifPresent(
                    existingPrimary -> {
                        existingPrimary.setIsPrimary(false);
                        bankAccountRepository.save(existingPrimary);
                        log.info("Demoted previous primary bank account: accountId={} for merchantId={}", 
                                existingPrimary.getId(), merchantId);
                    });
        }

        // 7. Build and persist entity initially in unverified state
        BankAccount bankAccount = BankAccount.builder()
                .merchant(merchant)
                .accountHolderName(request.getAccountHolderName())
                .accountNumberEncrypted(encryptedAccountNumber)
                .accountNumberHash(accountHash)
                .accountNumberLast4(last4)
                .accountNumberKeyVersion(1)
                .ifscCode(request.getIfscCode().toUpperCase().trim())
                .bankName(request.getBankName().trim())
                .isPrimary(makePrimary)
                .isVerified(false)
                .build();

        BankAccount saved = bankAccountRepository.save(bankAccount);
        log.info("Bank account created: accountId={} for merchantId={}. Triggering verification engine...", saved.getId(), merchantId);

        // 8. Trigger Verification Engine for Penny Drop
        try {
            VerificationResult verificationResult = verificationEngineService.verifyBankAccount(merchantId, saved.getId());
            log.info("Penny drop verification for accountId={}: success={}", saved.getId(), verificationResult.isSuccessful());
            saved = bankAccountRepository.findById(saved.getId()).orElse(saved);
        } catch (Exception e) {
            log.warn("Penny drop verification encountered an error for accountId={}: {}", saved.getId(), e.getMessage());
        }

        return bankAccountMapper.toResponse(saved);
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
        return bankAccountMapper.toResponse(bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException(accountId)));
    }

    @Override
    public BankAccountResponse setPrimaryBankAccount(Long merchantId, Long accountId) {

        BankAccount targetAccount = bankAccountRepository.findByIdAndMerchantId(accountId, merchantId)
                .orElseThrow(() -> new BankAccountNotFoundException(
                        String.format("Bank account with ID %d not found " +
                                "for merchant ID %d", accountId, merchantId)));

        // if target account is already primary, return it
        if (Boolean.TRUE.equals(targetAccount.getIsPrimary())) {
            return bankAccountMapper.toResponse(targetAccount);
        }

        // if merchant has multiple bank accounts, then check if expect this any one is primary or not
         bankAccountRepository.findByMerchantIdAndIsPrimaryTrue(merchantId).ifPresent(
                 currentPrimary -> {
                     currentPrimary.setIsPrimary(false);
                     bankAccountRepository.save(currentPrimary);
                     log.info("Demoted previous primary bank account: accountId={} for merchantId={}",
                             currentPrimary.getId(), merchantId);
                 });
         targetAccount.setIsPrimary(true);
        BankAccount saved = bankAccountRepository.save(targetAccount);
        log.info("Promoted new primary bank account: accountId={} for merchantId={}",
                 targetAccount.getId(), merchantId);

        return bankAccountMapper.toResponse(saved);
    }

    @Override
    public void deleteBankAccount(Long merchantId, Long accountId) {
        BankAccount bankAccount = bankAccountRepository.findByIdAndMerchantId(accountId , merchantId)
                .orElseThrow(
                        () -> new BankAccountNotFoundException(
                                String.format("Bank account with ID %d not found for merchant ID %d", accountId, merchantId)
                        ));


        if (Boolean.TRUE.equals(bankAccount.getIsPrimary())){
            long count = bankAccountRepository.countByMerchantId(merchantId);
            if (count > 1){
                throw new PrimaryBankAccountCannotBeDeletedException();
            }
        }

        bankAccountRepository.delete(bankAccount);
        log.info("Bank account deleted successfully: accountId={} for merchantId={}", accountId, merchantId);

        // Re-evaluate whether merchant still has any verified bank account
        boolean stillHasVerifiedBank = bankAccountRepository.findByMerchantId(merchantId)
                .stream()
                .anyMatch(ba -> !ba.getId().equals(accountId) && Boolean.TRUE.equals(ba.getIsVerified()));

        if (!stillHasVerifiedBank) {
            merchantRepository.findById(merchantId).ifPresent(m -> {
                m.setIsBankVerified(false);
                if (m.getStatus() == MerchantStatus.ACTIVE) {
                    m.setStatus(MerchantStatus.UNDER_REVIEW);
                    log.warn("Merchant {} demoted to UNDER_REVIEW because no verified bank account remains", merchantId);
                }
                merchantRepository.save(m);
            });
        }
    }

    @Override
    public BankAccountResponse verifyBankAccount(Long merchantId, Long accountId) {
        VerificationResult result = verificationEngineService.verifyBankAccount(merchantId, accountId);
        if (!result.isSuccessful()) {
            throw new BusinessException("Bank account verification failed: " + result.getFailureReason(), "BANK_VERIFICATION_FAILED");
        }
        BankAccount bankAccount = bankAccountRepository.findByIdAndMerchantId(accountId, merchantId)
                .orElseThrow(() -> new BankAccountNotFoundException(
                        String.format("Bank account with ID %d not found for merchant ID %d", accountId, merchantId)));
        return bankAccountMapper.toResponse(bankAccount);
    }
}
