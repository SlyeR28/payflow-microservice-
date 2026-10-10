package com.payflow.merchantservice.service.verification.impl;

import com.payflow.common.exceptions.BusinessException;
import com.payflow.common.exceptions.ResourceNotFoundException;
import com.payflow.merchantservice.model.entity.BankAccount;
import com.payflow.merchantservice.model.entity.Merchant;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import com.payflow.merchantservice.repository.BankAccountRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import com.payflow.merchantservice.service.verification.VerificationStrategy;
import com.payflow.merchantservice.service.verification.VerificationType;
import com.payflow.merchantservice.service.verification.dto.VerificationContext;
import com.payflow.merchantservice.service.verification.dto.VerificationResult;
import com.payflow.merchantservice.utils.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VerificationEngineService {

    private final Map<VerificationType, VerificationStrategy> strategyMap;
    private final MerchantRepository merchantRepository;
    private final BankAccountRepository bankAccountRepository;
    private final EncryptionUtil encryptionUtil;

    @Autowired
    public VerificationEngineService(List<VerificationStrategy> strategies,
                                     MerchantRepository merchantRepository,
                                     BankAccountRepository bankAccountRepository,
                                     EncryptionUtil encryptionUtil) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(VerificationStrategy::getVerificationType, Function.identity()));
        this.merchantRepository = merchantRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.encryptionUtil = encryptionUtil;
    }

    /**
     * Executes the verification strategy for the given type and persists the state.
     */
    @Transactional
    public VerificationResult verify(VerificationType type, VerificationContext context) {
        VerificationStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No verification strategy registered for: " + type);
        }

        strategy.validatePreConditions(context);
        VerificationResult result = strategy.execute(context);

        // Apply state updates to database upon verification success
        if (result.isSuccessful()) {
            applyVerificationSuccess(type, context, result);
        } else {
            log.warn("Verification failed for merchantId={}, type={}, reason={}",
                    context.getMerchantId(), type, result.getFailureReason());
        }

        return result;
    }

    /**
     * Convenience method to verify merchant PAN.
     */
    @Transactional
    public VerificationResult verifyMerchantPan(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found: " + merchantId));

        String decryptedPan = encryptionUtil.decrypt(merchant.getPanNumberEncrypted());

        VerificationContext context = VerificationContext.builder()
                .merchantId(merchant.getId())
                .userId(merchant.getUserId())
                .legalName(merchant.getLegalName())
                .businessName(merchant.getBusinessName())
                .businessType(merchant.getBusinessType())
                .panNumber(decryptedPan)
                .nameMatchThreshold(0.70)
                .build();

        return verify(VerificationType.PAN, context);
    }

    /**
     * Convenience method to run penny drop on a specific bank account.
     */
    @Transactional
    public VerificationResult verifyBankAccount(Long merchantId, Long bankAccountId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found: " + merchantId));

        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found: " + bankAccountId));

        if (!bankAccount.getMerchant().getId().equals(merchantId)) {
            throw new BusinessException("Bank account does not belong to this merchant", "ACCOUNT_MISMATCH");
        }

        String decryptedAccount = encryptionUtil.decrypt(bankAccount.getAccountNumberEncrypted());

        VerificationContext context = VerificationContext.builder()
                .merchantId(merchant.getId())
                .bankId(bankAccount.getId())
                .legalName(merchant.getLegalName())
                .accountHolderName(bankAccount.getAccountHolderName())
                .accountNumber(decryptedAccount)
                .ifscCode(bankAccount.getIfscCode())
                .nameMatchThreshold(0.70)
                .build();

        return verify(VerificationType.BANK_ACCOUNT, context);
    }

    private void applyVerificationSuccess(VerificationType type, VerificationContext context, VerificationResult result) {
        if (type == VerificationType.PAN) {
            merchantRepository.findById(context.getMerchantId()).ifPresent(m -> {
                m.setIsPanVerified(true);
                m.setPanVerifiedAt(Instant.now());
                checkAndActivateIfEligible(m);
                merchantRepository.save(m);
                log.info("Merchant PAN marked as VERIFIED: merchantId={}", m.getId());
            });
        } else if (type == VerificationType.BANK_ACCOUNT) {
            bankAccountRepository.findById(context.getBankId()).ifPresent(b -> {
                b.setIsVerified(true);
                b.setVerifiedAt(Instant.now());
                b.setNameMatchScore(result.getNameMatchScore());
                b.setBeneficiaryName(result.getRegisteredName());
                b.setVerificationReferenceId(result.getReferenceId());
                bankAccountRepository.save(b);
                log.info("Bank account marked as VERIFIED: accountId={}, score={}", b.getId(), result.getNameMatchScore());

                merchantRepository.findById(context.getMerchantId()).ifPresent(m -> {
                    m.setIsBankVerified(true);
                    m.setBankVerifiedAt(Instant.now());
                    checkAndActivateIfEligible(m);
                    merchantRepository.save(m);
                    log.info("Merchant Bank marked as VERIFIED: merchantId={}", m.getId());
                });
            });
        }
    }

    private void checkAndActivateIfEligible(Merchant m) {
        boolean panOk = Boolean.TRUE.equals(m.getIsPanVerified());
        boolean bankOk = Boolean.TRUE.equals(m.getIsBankVerified());

        if (panOk && bankOk) {
            if (m.getStatus() == MerchantStatus.PENDING || m.getStatus() == MerchantStatus.UNDER_REVIEW) {
                m.setStatus(MerchantStatus.ACTIVE);
                m.setApprovedAt(Instant.now());
                m.setRejectReason(null);
                log.info("Merchant automatically ACTIVATED because both PAN and Bank are verified: merchantId={}", m.getId());
            }
        }
    }
}
