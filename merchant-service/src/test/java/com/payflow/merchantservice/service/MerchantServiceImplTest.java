package com.payflow.merchantservice.service;

import com.payflow.merchantservice.exceptions.InvalidMerchantStateException;
import com.payflow.merchantservice.exceptions.MerchantNotFoundException;
import com.payflow.merchantservice.mapper.MerchantAddressMapper;
import com.payflow.merchantservice.mapper.MerchantMapper;
import com.payflow.merchantservice.model.entity.BankAccount;
import com.payflow.merchantservice.model.entity.Merchant;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import com.payflow.merchantservice.payload.responseDto.MerchantResponse;
import com.payflow.merchantservice.repository.BankAccountRepository;
import com.payflow.merchantservice.repository.MerchantAddressRepository;
import com.payflow.merchantservice.repository.MerchantKycRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import com.payflow.merchantservice.security.service.SecurityUtil;
import com.payflow.merchantservice.service.impl.MerchantServiceImpl;
import com.payflow.merchantservice.service.verification.impl.VerificationEngineService;
import com.payflow.merchantservice.utils.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceImplTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private MerchantMapper merchantMapper;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private MerchantKycRepository merchantKycRepository;

    @Mock
    private MerchantAddressRepository merchantAddressRepository;

    @Mock
    private MerchantAddressMapper merchantAddressMapper;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private VerificationEngineService verificationEngineService;

    @InjectMocks
    private MerchantServiceImpl merchantService;

    private Merchant merchant;

    @BeforeEach
    void setUp() {
        merchant = Merchant.builder()
                .id(1L)
                .userId(101L)
                .businessName("Acme Corp")
                .legalName("Acme Corporation Pvt Ltd")
                .status(MerchantStatus.PENDING)
                .isPanVerified(false)
                .isBankVerified(false)
                .build();
    }

    @Test
    @DisplayName("approve: should fail when PAN is not verified")
    void approve_FailsWhenPanNotVerified() {
        when(merchantRepository.findById(1L)).thenReturn(Optional.of(merchant));

        InvalidMerchantStateException ex = assertThrows(InvalidMerchantStateException.class, () ->
                merchantService.approve(1L, 999L));

        assertTrue(ex.getMessage().contains("PAN is not verified"));
        assertEquals(MerchantStatus.PENDING, merchant.getStatus());
        verify(merchantRepository, never()).save(any());
    }

    @Test
    @DisplayName("approve: should fail when PAN is verified but bank account is not verified")
    void approve_FailsWhenBankNotVerified() {
        merchant.setIsPanVerified(true);
        when(merchantRepository.findById(1L)).thenReturn(Optional.of(merchant));
        when(bankAccountRepository.findByMerchantId(1L)).thenReturn(List.of());

        InvalidMerchantStateException ex = assertThrows(InvalidMerchantStateException.class, () ->
                merchantService.approve(1L, 999L));

        assertTrue(ex.getMessage().contains("Bank account is not verified"));
        assertEquals(MerchantStatus.PENDING, merchant.getStatus());
        verify(merchantRepository, never()).save(any());
    }

    @Test
    @DisplayName("approve: should succeed when both PAN and Bank are verified")
    void approve_SucceedsWhenBothPanAndBankVerified() {
        merchant.setIsPanVerified(true);
        merchant.setIsBankVerified(true);
        when(merchantRepository.findById(1L)).thenReturn(Optional.of(merchant));
        when(merchantMapper.toResponse(any(Merchant.class))).thenReturn(MerchantResponse.builder()
                .id(1L)
                .status(MerchantStatus.ACTIVE)
                .isPanVerified(true)
                .isBankVerified(true)
                .build());

        MerchantResponse response = merchantService.approve(1L, 999L);

        assertNotNull(response);
        assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
        assertNotNull(merchant.getApprovedAt());
        assertEquals(999L, merchant.getApprovedBy());
        verify(merchantRepository).save(merchant);
    }
}
