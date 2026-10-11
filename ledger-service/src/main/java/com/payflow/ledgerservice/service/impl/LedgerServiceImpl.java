package com.payflow.ledgerservice.service.impl;

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
import com.payflow.ledgerservice.model.entity.LedgerPosting;
import com.payflow.ledgerservice.model.enums.AccountStatus;
import com.payflow.ledgerservice.model.enums.AccountType;
import com.payflow.ledgerservice.model.enums.PostingDirection;
import com.payflow.ledgerservice.repository.AccountBalanceRepository;
import com.payflow.ledgerservice.repository.AccountRepository;
import com.payflow.ledgerservice.repository.JournalEntryRepository;
import com.payflow.ledgerservice.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

    private final AccountRepository accountRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final LedgerMapper ledgerMapper;


    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
         String accountNumber = generateAccountNumber(request);

         if (accountRepository.existsByAccountNumber(accountNumber)){
             throw new DuplicateResourceException("Account" , accountNumber ,accountNumber);
         }

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .name(request.getName())
                .merchantId(request.getMerchantId())
                .accountType(request.getAccountType())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .status(AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);

        // initialize snapshot balance to 0.0000
        AccountBalance balance = AccountBalance.builder()
                .account(savedAccount)
                .currentBalance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .pendingBalance(BigDecimal.ZERO)
                .build();

        accountBalanceRepository.save(balance);
        savedAccount.setAccountBalance(balance);

        log.info("Created ledger account [{}] for merchant [{}]" , accountNumber ,request.getMerchantId());

        return ledgerMapper.toAccountResponse(savedAccount);
    }

    @Override
    @Transactional
    public JournalEntryResponse recordTransactions(RecordTransactionRequest request) {

      // step 1 Idempotency check
        Optional<JournalEntry> existing = journalEntryRepository
                                            .findByIdempotencyKey(request.getIdempotencyKey());


        if (existing.isPresent()){
            log.info("Idempotent hit: transaction with key [{}] already processed" , request.getIdempotencyKey());
            return ledgerMapper.toJournalEntryResponse(existing.get());
        }

       // step 2 validate Double-Entry Invariant
        validateZeroSum(request);

        // step 3 Fetch and Validate All account involved
        Map<String , Account> accountMap = new HashMap<>();

        for (PostingDto postingDto: request.getPostings()){
            Account account = accountMap.computeIfAbsent(postingDto.getAccountNumber(), accNum -> accountRepository
                    .findByAccountNumber(accNum).orElseThrow(
                            () -> new AccountNotFoundException(accNum)
                    ));

            if (account.getStatus() != AccountStatus.ACTIVE){
                throw new AccountInactiveException(account.getAccountNumber() , account.getStatus());
            }
        }

        // step 4 Build immutable Journal Entry and Postings
        JournalEntry journalEntry = JournalEntry.builder()
                .sourceSystem(request.getSourceSystem())
                .entryType(request.getEntryType())
                .description(request.getDescription())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        for (PostingDto dto : request.getPostings()){
            Account account = accountMap.get(dto.getAccountNumber());
            LedgerPosting posting = LedgerPosting.builder()
                    .account(account)
                    .direction(dto.getDirection())
                    .amount(dto.getAmount())
                    .currency(dto.getCurrency() != null ? dto.getCurrency() : "INR")
                    .build();
            journalEntry.addPosting(posting);
        }

        JournalEntry savedEntry = journalEntryRepository.save(journalEntry);

        // step 5 Update running AccountBalance snapshots with Optimistic Locking
        // Step 5: Update running AccountBalance snapshots with Optimistic Locking
        for (PostingDto dto : request.getPostings()) {
            Account account = accountMap.get(dto.getAccountNumber());
            applyBalanceSnapshot(account, dto.getDirection(), dto.getAmount());
        }
        log.info("Successfully posted journal transaction [{}] with [{}] postings",
                savedEntry.getEntryReference(), request.getPostings().size());
        return ledgerMapper.toJournalEntryResponse(savedEntry);
    }

    @Override
    public AccountResponse getAccountByNumber(String accountNumber) {
        return ledgerMapper.toAccountResponse(accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber)));
    }

    @Override
    public AccountResponse getAccountByMerchantId(Long merchantId) {
        return ledgerMapper.toAccountResponse(accountRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new AccountNotFoundException("Merchant Id: " + merchantId)));
    }

    @Override
    public JournalEntryResponse getTransactionsByReference(String reference) {
        return ledgerMapper.toJournalEntryResponse(journalEntryRepository.findByEntryReference(reference)
                .orElseThrow(() -> new AccountNotFoundException("Entry Reference: " + reference)));
    }

    private void validateZeroSum(RecordTransactionRequest request) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (PostingDto p : request.getPostings()){
            if (p.getDirection() == PostingDirection.DEBIT){
                totalDebit = totalDebit.add(p.getAmount());
            } else if (p.getDirection() == PostingDirection.CREDIT) {
                totalCredit = totalCredit.add(p.getAmount());
                
            }
        }

        if (totalDebit.compareTo(totalCredit) != 0){
            throw new UnbalancedJournalEntryException(totalDebit , totalCredit);
        }

    }

    private void applyBalanceSnapshot(Account account, PostingDirection direction, BigDecimal amount) {
        AccountBalance balance = accountBalanceRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AccountNotFoundException("Balance not found for account " + account.getAccountNumber()));
        BigDecimal delta = calculateBalanceDelta(account.getAccountType(), direction, amount);
        BigDecimal newCurrentBalance = balance.getCurrentBalance().add(delta);
        BigDecimal newAvailableBalance = balance.getAvailableBalance().add(delta);
        // Prevent negative balances on merchant liability accounts if debit overdraft not allowed
        if (account.getAccountType() == AccountType.LIABILITY && newAvailableBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException(account.getAccountNumber(), balance.getAvailableBalance(), amount);
        }
        balance.setCurrentBalance(newCurrentBalance);
        balance.setAvailableBalance(newAvailableBalance);
        accountBalanceRepository.save(balance);
    }

    /**
     * Accounting Arithmetic:
     * - ASSET / EXPENSE: Normal balance is DEBIT (+). DEBIT increases, CREDIT decreases.
     * - LIABILITY / REVENUE / EQUITY: Normal balance is CREDIT (+). CREDIT increases, DEBIT decreases.
     */
    private BigDecimal calculateBalanceDelta(AccountType accountType, PostingDirection direction, BigDecimal amount) {
        if (accountType == AccountType.ASSET || accountType == AccountType.EXPENSE) {
            return direction == PostingDirection.DEBIT ? amount : amount.negate();
        } else {
            return direction == PostingDirection.CREDIT ? amount : amount.negate();
        }
    }

    private String generateAccountNumber(CreateAccountRequest request) {
        if (request.getMerchantId() != null){
            return "ACC-M" + request.getMerchantId() + "-" +(request.getCurrency()!=null ?
                    request.getCurrency():"INR");
        }
        return "ACC-SYS-" + UUID.randomUUID().toString().substring(0 , 8).toUpperCase();
    }
}
