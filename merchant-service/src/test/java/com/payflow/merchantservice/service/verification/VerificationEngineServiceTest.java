package com.payflow.merchantservice.service.verification;

import com.payflow.merchantservice.model.entity.BankAccount;
import com.payflow.merchantservice.model.entity.Merchant;
import com.payflow.merchantservice.model.enums.BusinessType;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import com.payflow.merchantservice.repository.BankAccountRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import com.payflow.merchantservice.service.verification.dto.PanVerificationResult;
import com.payflow.merchantservice.service.verification.dto.PennyDropResult;
import com.payflow.merchantservice.service.verification.dto.VerificationResult;
import com.payflow.merchantservice.service.verification.impl.BankAccountVerificationStrategy;
import com.payflow.merchantservice.service.verification.impl.PanVerificationStrategy;
import com.payflow.merchantservice.service.verification.impl.VerificationEngineService;
import com.payflow.merchantservice.utils.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationEngineServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private PanVerificationStrategy panVerificationStrategy;

    @Mock
    private BankAccountVerificationStrategy bankAccountVerificationStrategy;

    private VerificationEngineService verificationEngineService;

    private Merchant merchant;
    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        when(panVerificationStrategy.getVerificationType()).thenReturn(VerificationType.PAN);
        when(bankAccountVerificationStrategy.getVerificationType()).thenReturn(VerificationType.BANK_ACCOUNT);

        verificationEngineService = new VerificationEngineService(
                List.of(panVerificationStrategy, bankAccountVerificationStrategy),
                merchantRepository,
                bankAccountRepository,
                encryptionUtil
        );

        merchant = Merchant.builder()
                .id(1L)
                .userId(101L)
                .legalName("Rishabh Private Limited")
                .businessName("PayFlow Tech")
                .businessType(BusinessType.PRIVATE_LIMITED)
                .panNumberEncrypted("enc_pan")
                .status(MerchantStatus.PENDING)
                .isPanVerified(false)
                .isBankVerified(false)
                .build();

        bankAccount = BankAccount.builder()
                .id(10L)
                .merchant(merchant)
                .accountHolderName("Rishabh Private Limited")
                .accountNumberEncrypted("enc_acc")
                .ifscCode("HDFC0001234")
                .isVerified(false)
                .build();
    }

    @Test
    @DisplayName("verifyMerchantPan: updates isPanVerified, does NOT activate if bank is not verified")
    void verifyPan_Success_DoesNotActivateWithoutBank() {
        when(merchantRepository.findById(1L)).thenReturn(Optional.of(merchant));
        when(encryptionUtil.decrypt("enc_pan")).thenReturn("ABCDE1234F");

        PanVerificationResult panResult = PanVerificationResult.builder()
                .verificationType(VerificationType.PAN)
                .successful(true)
                .nameMatched(true)
                .nameMatchScore(1.0)
                .registeredName("Rishabh Private Limited")
                .build();

        when(panVerificationStrategy.execute(any())).thenReturn(panResult);

        VerificationResult result = verificationEngineService.verifyMerchantPan(1L);

        assertTrue(result.isSuccessful());
        assertTrue(merchant.getIsPanVerified());
        assertNotNull(merchant.getPanVerifiedAt());
        assertFalse(merchant.getIsBankVerified());
        assertEquals(MerchantStatus.PENDING, merchant.getStatus());
        verify(merchantRepository).save(merchant);
    }

    @Test
    @DisplayName("verifyBankAccount: activates merchant when bank is verified and PAN is already verified")
    void verifyBankAccount_Success_ActivatesMerchantWhenBothVerified() {
        // Merchant has PAN verified already
        merchant.setIsPanVerified(true);

        when(merchantRepository.findById(1L)).thenReturn(Optional.of(merchant));
        when(bankAccountRepository.findById(10L)).thenReturn(Optional.of(bankAccount));
        when(encryptionUtil.decrypt("enc_acc")).thenReturn("50100456789123");

        PennyDropResult pennyResult = PennyDropResult.builder()
                .verificationType(VerificationType.BANK_ACCOUNT)
                .successful(true)
                .nameMatched(true)
                .nameMatchScore(1.0)
                .registeredName("Rishabh Private Limited")
                .referenceId("SIM-PENNY-123")
                .build();

        when(bankAccountVerificationStrategy.execute(any())).thenReturn(pennyResult);

        VerificationResult result = verificationEngineService.verifyBankAccount(1L, 10L);

        assertTrue(result.isSuccessful());
        assertTrue(bankAccount.getIsVerified());
        assertTrue(merchant.getIsBankVerified());
        // Both are verified now -> merchant automatically activated!
        assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
        assertNotNull(merchant.getApprovedAt());
        verify(bankAccountRepository).save(bankAccount);
        verify(merchantRepository).save(merchant);
    }
}
