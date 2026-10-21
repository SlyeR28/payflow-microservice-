package com.payflow.ledgerservice.service;

import com.payflow.common.exceptions.DuplicateResourceException;
import com.payflow.ledgerservice.dto.request.CreateAccountRequest;
import com.payflow.ledgerservice.dto.request.PostingDto;
import com.payflow.ledgerservice.dto.request.RecordTransactionRequest;
import com.payflow.ledgerservice.dto.response.AccountResponse;
import com.payflow.ledgerservice.dto.response.JournalEntryResponse;
import com.payflow.ledgerservice.exception.AccountInactiveException;
import com.payflow.ledgerservice.exception.AccountNotFoundException;
import com.payflow.ledgerservice.exception.InsufficientBalanceException;
import com.payflow.ledgerservice.exception.UnbalancedJournalEntryException;
import com.payflow.ledgerservice.mapper.LedgerMapper;
import com.payflow.ledgerservice.model.entity.Account;
import com.payflow.ledgerservice.model.entity.AccountBalance;
import com.payflow.ledgerservice.model.entity.JournalEntry;
import com.payflow.ledgerservice.model.enums.AccountStatus;
import com.payflow.ledgerservice.model.enums.AccountType;
import com.payflow.ledgerservice.model.enums.EntryType;
import com.payflow.ledgerservice.model.enums.PostingDirection;
import com.payflow.ledgerservice.repository.AccountBalanceRepository;
import com.payflow.ledgerservice.repository.AccountRepository;
import com.payflow.ledgerservice.repository.JournalEntryRepository;
import com.payflow.ledgerservice.service.impl.LedgerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountBalanceRepository accountBalanceRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private LedgerMapper ledgerMapper;
    private LedgerServiceImpl ledgerService;

    @BeforeEach
    void setUp() {
        ledgerMapper = Mappers.getMapper(LedgerMapper.class);
        ledgerService = new LedgerServiceImpl(
                accountRepository,
                accountBalanceRepository,
                journalEntryRepository,
                ledgerMapper
        );
    }

    @Test
    @DisplayName("createAccount: Should create merchant account with zero balance initialization")
    void createAccount_MerchantAccount_Success() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .name("Merchant Primary INR")
                .merchantId(101L)
                .accountType(AccountType.LIABILITY)
                .currency("INR")
                .build();

        String expectedAccNum = "ACC-M101-INR";
        when(accountRepository.existsByAccountNumber(expectedAccNum)).thenReturn(false);

        Account savedAccount = Account.builder()
                .id(1L)
                .accountNumber(expectedAccNum)
                .name(request.getName())
                .merchantId(101L)
                .accountType(AccountType.LIABILITY)
                .currency("INR")
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        AccountResponse response = ledgerService.createAccount(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccountNumber()).isEqualTo(expectedAccNum);
        assertThat(response.getMerchantId()).isEqualTo(101L);
        assertThat(response.getAccountType()).isEqualTo(AccountType.LIABILITY);

        verify(accountBalanceRepository, times(1)).save(any(AccountBalance.class));
    }

    @Test
    @DisplayName("createAccount: Should throw DuplicateResourceException when account number exists")
    void createAccount_DuplicateAccountNumber_ThrowsException() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .name("Duplicate Account")
                .merchantId(101L)
                .accountType(AccountType.LIABILITY)
                .currency("INR")
                .build();

        when(accountRepository.existsByAccountNumber("ACC-M101-INR")).thenReturn(true);

        assertThatThrownBy(() -> ledgerService.createAccount(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(accountRepository, never()).save(any(Account.class));
        verify(accountBalanceRepository, never()).save(any(AccountBalance.class));
    }

    @Test
    @DisplayName("recordTransactions: Should return existing journal entry on idempotent hit")
    void recordTransactions_IdempotentHit_ReturnsCachedEntry() {
        String idempotencyKey = "tx-idemp-12345";
        RecordTransactionRequest request = RecordTransactionRequest.builder()
                .sourceSystem("PAYMENT_SERVICE")
                .entryType(EntryType.PAYMENT_SETTLEMENT)
                .idempotencyKey(idempotencyKey)
                .postings(List.of(
                        PostingDto.builder().accountNumber("ACC-SYS-CLEARING").direction(PostingDirection.DEBIT).amount(new BigDecimal("100.00")).build(),
                        PostingDto.builder().accountNumber("ACC-M101-INR").direction(PostingDirection.CREDIT).amount(new BigDecimal("100.00")).build()
                ))
                .build();

        JournalEntry existing = JournalEntry.builder()
                .id(1L)
                .entryReference("JRN-EXISTING-999")
                .idempotencyKey(idempotencyKey)
                .sourceSystem("PAYMENT_SERVICE")
                .entryType(EntryType.PAYMENT_SETTLEMENT)
                .build();

        when(journalEntryRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existing));

        JournalEntryResponse response = ledgerService.recordTransactions(request);

        assertThat(response).isNotNull();
        assertThat(response.getEntryReference()).isEqualTo("JRN-EXISTING-999");
        verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        verify(accountRepository, never()).findByAccountNumber(anyString());
    }

    @Test
    @DisplayName("recordTransactions: Should throw UnbalancedJournalEntryException when debits do not equal credits")
    void recordTransactions_UnbalancedJournalEntry_ThrowsException() {
        RecordTransactionRequest request = RecordTransactionRequest.builder()
                .sourceSystem("PAYMENT_SERVICE")
                .entryType(EntryType.PAYMENT_SETTLEMENT)
                .idempotencyKey("tx-unbalanced-001")
                .postings(List.of(
                        PostingDto.builder().accountNumber("ACC-SYS-CLEARING").direction(PostingDirection.DEBIT).amount(new BigDecimal("100.00")).build(),
                        PostingDto.builder().accountNumber("ACC-M101-INR").direction(PostingDirection.CREDIT).amount(new BigDecimal("50.00")).build()
                ))
                .build();

        when(journalEntryRepository.findByIdempotencyKey("tx-unbalanced-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ledgerService.recordTransactions(request))
                .isInstanceOf(UnbalancedJournalEntryException.class);

        verify(journalEntryRepository, never()).save(any(JournalEntry.class));
    }

    @Test
    @DisplayName("recordTransactions: Should throw AccountNotFoundException when posting account does not exist")
    void recordTransactions_AccountNotFound_ThrowsException() {
        RecordTransactionRequest request = RecordTransactionRequest.builder()
                .sourceSystem("PAYMENT_SERVICE")
                .entryType(EntryType.PAYMENT_SETTLEMENT)
                .idempotencyKey("tx-acc-missing")
                .postings(List.of(
                        PostingDto.builder().accountNumber("ACC-SYS-CLEARING").direction(PostingDirection.DEBIT).amount(new BigDecimal("100.00")).build(),
                        PostingDto.builder().accountNumber("ACC-NONEXISTENT").direction(PostingDirection.CREDIT).amount(new BigDecimal("100.00")).build()
                ))
                .build();

        when(journalEntryRepository.findByIdempotencyKey("tx-acc-missing")).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("ACC-SYS-CLEARING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ledgerService.recordTransactions(request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("recordTransactions: Should throw AccountInactiveException when account is frozen")
    void recordTransactions_AccountInactive_ThrowsException() {
        RecordTransactionRequest request = RecordTransactionRequest.builder()
                .sourceSystem("PAYMENT_SERVICE")
                .entryType(EntryType.PAYMENT_SETTLEMENT)
                .idempotencyKey("tx-acc-suspended")
                .postings(List.of(
                        PostingDto.builder().accountNumber("ACC-SYS-CLEARING").direction(PostingDirection.DEBIT).amount(new BigDecimal("100.00")).build(),
                        PostingDto.builder().accountNumber("ACC-M101-INR").direction(PostingDirection.CREDIT).amount(new BigDecimal("100.00")).build()
                ))
                .build();

        Account clearingAcc = Account.builder()
                .id(1L)
                .accountNumber("ACC-SYS-CLEARING")
                .status(AccountStatus.FROZEN)
                .build();

        when(journalEntryRepository.findByIdempotencyKey("tx-acc-suspended")).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("ACC-SYS-CLEARING")).thenReturn(Optional.of(clearingAcc));

        assertThatThrownBy(() -> ledgerService.recordTransactions(request))
                .isInstanceOf(AccountInactiveException.class);
    }

    @Test
    @DisplayName("recordTransactions: Should throw InsufficientBalanceException when liability account debit causes negative balance")
    void recordTransactions_InsufficientBalance_ThrowsException() {
        RecordTransactionRequest request = RecordTransactionRequest.builder()
                .sourceSystem("PAYMENT_SERVICE")
                .entryType(EntryType.MERCHANT_PAYOUT)
                .idempotencyKey("tx-payout-overdraft")
                .postings(List.of(
                        PostingDto.builder().accountNumber("ACC-M101-INR").direction(PostingDirection.DEBIT).amount(new BigDecimal("500.00")).build(),
                        PostingDto.builder().accountNumber("ACC-BANK-SETTLEMENT").direction(PostingDirection.CREDIT).amount(new BigDecimal("500.00")).build()
                ))
                .build();

        Account merchantAcc = Account.builder()
                .id(1L)
                .accountNumber("ACC-M101-INR")
                .accountType(AccountType.LIABILITY)
                .status(AccountStatus.ACTIVE)
                .build();

        Account bankAcc = Account.builder()
                .id(2L)
                .accountNumber("ACC-BANK-SETTLEMENT")
                .accountType(AccountType.ASSET)
                .status(AccountStatus.ACTIVE)
                .build();

        AccountBalance merchantBalance = AccountBalance.builder()
                .id(1L)
                .account(merchantAcc)
                .currentBalance(new BigDecimal("100.00"))
                .availableBalance(new BigDecimal("100.00"))
                .pendingBalance(BigDecimal.ZERO)
                .build();

        when(journalEntryRepository.findByIdempotencyKey("tx-payout-overdraft")).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("ACC-M101-INR")).thenReturn(Optional.of(merchantAcc));
        when(accountRepository.findByAccountNumber("ACC-BANK-SETTLEMENT")).thenReturn(Optional.of(bankAcc));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountBalanceRepository.findByAccountId(1L)).thenReturn(Optional.of(merchantBalance));

        assertThatThrownBy(() -> ledgerService.recordTransactions(request))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("recordTransactions: Balanced double entry should persist journal and update account balance snapshots")
    void recordTransactions_BalancedEntry_SuccessfullyPostsAndUpdatesBalances() {
        RecordTransactionRequest request = RecordTransactionRequest.builder()
                .sourceSystem("PAYMENT_GATEWAY")
                .entryType(EntryType.PAYMENT_SETTLEMENT)
                .idempotencyKey("tx-capture-success-001")
                .description("Customer payment capture")
                .postings(List.of(
                        PostingDto.builder().accountNumber("ACC-ASSET-CLEARING").direction(PostingDirection.DEBIT).amount(new BigDecimal("1000.00")).currency("INR").build(),
                        PostingDto.builder().accountNumber("ACC-LIABILITY-MERCHANT").direction(PostingDirection.CREDIT).amount(new BigDecimal("1000.00")).currency("INR").build()
                ))
                .build();

        Account assetAcc = Account.builder()
                .id(1L)
                .accountNumber("ACC-ASSET-CLEARING")
                .accountType(AccountType.ASSET)
                .status(AccountStatus.ACTIVE)
                .currency("INR")
                .build();

        Account liabilityAcc = Account.builder()
                .id(2L)
                .accountNumber("ACC-LIABILITY-MERCHANT")
                .accountType(AccountType.LIABILITY)
                .status(AccountStatus.ACTIVE)
                .currency("INR")
                .build();

        AccountBalance assetBalance = AccountBalance.builder()
                .id(1L)
                .account(assetAcc)
                .currentBalance(new BigDecimal("500.00"))
                .availableBalance(new BigDecimal("500.00"))
                .pendingBalance(BigDecimal.ZERO)
                .build();

        AccountBalance liabilityBalance = AccountBalance.builder()
                .id(2L)
                .account(liabilityAcc)
                .currentBalance(new BigDecimal("2000.00"))
                .availableBalance(new BigDecimal("2000.00"))
                .pendingBalance(BigDecimal.ZERO)
                .build();

        when(journalEntryRepository.findByIdempotencyKey("tx-capture-success-001")).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("ACC-ASSET-CLEARING")).thenReturn(Optional.of(assetAcc));
        when(accountRepository.findByAccountNumber("ACC-LIABILITY-MERCHANT")).thenReturn(Optional.of(liabilityAcc));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(inv -> {
            JournalEntry je = inv.getArgument(0);
            je.setId(10L);
            je.setEntryReference("JRN-REF-001");
            return je;
        });

        when(accountBalanceRepository.findByAccountId(1L)).thenReturn(Optional.of(assetBalance));
        when(accountBalanceRepository.findByAccountId(2L)).thenReturn(Optional.of(liabilityBalance));

        JournalEntryResponse response = ledgerService.recordTransactions(request);

        assertThat(response).isNotNull();
        assertThat(response.getEntryReference()).isEqualTo("JRN-REF-001");
        assertThat(response.isBalanced()).isTrue();
        assertThat(response.getTotalDebit()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.getTotalCredit()).isEqualByComparingTo(new BigDecimal("1000.00"));

        // Asset normal balance is debit (+): 500 + 1000 = 1500
        assertThat(assetBalance.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        // Liability normal balance is credit (+): 2000 + 1000 = 3000
        assertThat(liabilityBalance.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("3000.00"));

        verify(accountBalanceRepository, times(2)).save(any(AccountBalance.class));
    }

    @Test
    @DisplayName("getAccountByNumber: Success")
    void getAccountByNumber_Success() {
        Account account = Account.builder()
                .id(1L)
                .accountNumber("ACC-M101-INR")
                .name("Merchant Account")
                .merchantId(101L)
                .accountType(AccountType.LIABILITY)
                .status(AccountStatus.ACTIVE)
                .currency("INR")
                .build();

        AccountBalance balance = AccountBalance.builder()
                .account(account)
                .currentBalance(new BigDecimal("750.00"))
                .availableBalance(new BigDecimal("750.00"))
                .pendingBalance(BigDecimal.ZERO)
                .build();
        account.setAccountBalance(balance);

        when(accountRepository.findByAccountNumber("ACC-M101-INR")).thenReturn(Optional.of(account));

        AccountResponse response = ledgerService.getAccountByNumber("ACC-M101-INR");

        assertThat(response).isNotNull();
        assertThat(response.getAccountNumber()).isEqualTo("ACC-M101-INR");
        assertThat(response.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("750.00"));
    }

    @Test
    @DisplayName("getAccountByNumber: Throws AccountNotFoundException")
    void getAccountByNumber_NotFound() {
        when(accountRepository.findByAccountNumber("ACC-MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ledgerService.getAccountByNumber("ACC-MISSING"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("getAccountByMerchantId: Success")
    void getAccountByMerchantId_Success() {
        Account account = Account.builder()
                .id(2L)
                .accountNumber("ACC-M202-INR")
                .merchantId(202L)
                .accountType(AccountType.LIABILITY)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findByMerchantId(202L)).thenReturn(Optional.of(account));

        AccountResponse response = ledgerService.getAccountByMerchantId(202L);

        assertThat(response).isNotNull();
        assertThat(response.getMerchantId()).isEqualTo(202L);
    }

    @Test
    @DisplayName("getTransactionsByReference: Success")
    void getTransactionsByReference_Success() {
        JournalEntry journalEntry = JournalEntry.builder()
                .id(5L)
                .entryReference("JRN-REF-777")
                .sourceSystem("PAYMENT_SERVICE")
                .entryType(EntryType.REFUND_REVERSAL)
                .description("Refund transaction")
                .build();

        when(journalEntryRepository.findByEntryReference("JRN-REF-777")).thenReturn(Optional.of(journalEntry));

        JournalEntryResponse response = ledgerService.getTransactionsByReference("JRN-REF-777");

        assertThat(response).isNotNull();
        assertThat(response.getEntryReference()).isEqualTo("JRN-REF-777");
        assertThat(response.getEntryType()).isEqualTo(EntryType.REFUND_REVERSAL);
    }
}
