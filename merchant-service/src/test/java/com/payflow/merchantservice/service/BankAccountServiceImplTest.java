package com.payflow.merchantservice.service;

import com.payflow.common.exceptions.BusinessException;
import com.payflow.common.exceptions.DuplicateResourceException;
import com.payflow.merchantservice.exceptions.PrimaryBankAccountCannotBeDeletedException;
import com.payflow.merchantservice.mapper.BankAccountMapper;
import com.payflow.merchantservice.model.entity.BankAccount;
import com.payflow.merchantservice.model.entity.Merchant;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import com.payflow.merchantservice.payload.requestDto.AddBankAccountRequest;
import com.payflow.merchantservice.payload.responseDto.BankAccountResponse;
import com.payflow.merchantservice.repository.BankAccountRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import com.payflow.merchantservice.service.impl.BankAccountServiceImpl;
import com.payflow.merchantservice.utils.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankAccountMapper bankAccountMapper;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private com.payflow.merchantservice.service.verification.impl.VerificationEngineService verificationEngineService;

    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    private Merchant activeMerchant;
    private AddBankAccountRequest request;

    @BeforeEach
    void setUp() {
        activeMerchant = Merchant.builder()
                .id(1L)
                .userId(100L)
                .businessName("Test PayFlow Merchant")
                .status(MerchantStatus.ACTIVE)
                .build();

        request = AddBankAccountRequest.builder()
                .accountHolderName("Rishabh Kumar")
                .accountNumber("50100456789123")
                .ifscCode("HDFC0001234")
                .bankName("HDFC Bank")
                .isPrimary(true)
                .build();
    }

    @Test
    void addBankAccount_Success_SimulatedVerification() {
        when(merchantRepository.findById(1L)).thenReturn(Optional.of(activeMerchant));
        when(bankAccountRepository.countByMerchantId(1L)).thenReturn(0L);
        when(bankAccountRepository.findByMerchantIdAndAccountNumberHash(eq(1L), anyString())).thenReturn(Optional.empty());
        when(bankAccountRepository.countByAccountNumberHash(anyString())).thenReturn(0L);
        when(encryptionUtil.encrypt("50100456789123")).thenReturn("encryptedAccountToken");

        BankAccount savedEntity = BankAccount.builder()
                .id(10L)
                .merchant(activeMerchant)
                .accountHolderName(request.getAccountHolderName())
                .accountNumberLast4("9123")
                .isPrimary(true)
                .isVerified(true)
                .build();

        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(savedEntity);
        when(bankAccountRepository.findById(10L)).thenReturn(Optional.of(savedEntity));
        when(verificationEngineService.verifyBankAccount(eq(1L), any()))
                .thenReturn(com.payflow.merchantservice.service.verification.dto.PennyDropResult.builder()
                        .successful(true)
                        .nameMatched(true)
                        .nameMatchScore(100.0)
                        .build());

        BankAccountResponse expectedResponse = BankAccountResponse.builder()
                .id(10L)
                .accountHolderName(request.getAccountHolderName())
                .accountNumberLast4("9123")
                .isPrimary(true)
                .isVerified(true)
                .build();
        when(bankAccountMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        BankAccountResponse response = bankAccountService.addBankAccount(1L, request);

        assertNotNull(response);
        assertEquals("9123", response.getAccountNumberLast4());
        assertTrue(response.getIsVerified());
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void addBankAccount_DuplicateAccount_ThrowsDuplicateResourceException() {
        when(merchantRepository.findById(1L)).thenReturn(Optional.of(activeMerchant));
        when(bankAccountRepository.countByMerchantId(1L)).thenReturn(1L);
        when(bankAccountRepository.findByMerchantIdAndAccountNumberHash(eq(1L), anyString()))
                .thenReturn(Optional.of(new BankAccount()));

        assertThrows(DuplicateResourceException.class, () -> bankAccountService.addBankAccount(1L, request));
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void addBankAccount_QuotaExceeded_ThrowsBusinessException() {
        when(merchantRepository.findById(1L)).thenReturn(Optional.of(activeMerchant));
        when(bankAccountRepository.countByMerchantId(1L)).thenReturn(5L);

        assertThrows(BusinessException.class, () -> bankAccountService.addBankAccount(1L, request));
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void deleteBankAccount_PrimaryAccountWithMultiple_ThrowsException() {
        BankAccount primaryAccount = BankAccount.builder()
                .id(10L)
                .isPrimary(true)
                .merchant(activeMerchant)
                .build();

        when(bankAccountRepository.findByIdAndMerchantId(10L, 1L)).thenReturn(Optional.of(primaryAccount));
        when(bankAccountRepository.countByMerchantId(1L)).thenReturn(2L);

        assertThrows(PrimaryBankAccountCannotBeDeletedException.class,
                () -> bankAccountService.deleteBankAccount(1L, 10L));
        verify(bankAccountRepository, never()).delete(any());
    }
}
