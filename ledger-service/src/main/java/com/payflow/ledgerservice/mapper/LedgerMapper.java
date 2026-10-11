package com.payflow.ledgerservice.mapper;

import com.payflow.ledgerservice.dto.response.AccountResponse;
import com.payflow.ledgerservice.dto.response.JournalEntryResponse;
import com.payflow.ledgerservice.dto.response.PostingResponse;
import com.payflow.ledgerservice.model.entity.Account;
import com.payflow.ledgerservice.model.entity.JournalEntry;
import com.payflow.ledgerservice.model.entity.LedgerPosting;
import com.payflow.ledgerservice.model.enums.PostingDirection;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface LedgerMapper {

    @Mapping(target = "currentBalance", source = "accountBalance.currentBalance")
    @Mapping(target = "availableBalance", source = "accountBalance.availableBalance")
    @Mapping(target = "pendingBalance", source = "accountBalance.pendingBalance")
    AccountResponse toAccountResponse(Account account);

    @Mapping(target = "accountNumber", source = "account.accountNumber")
    @Mapping(target = "accountName", source = "account.name")
    PostingResponse toPostingResponse(LedgerPosting posting);

    List<PostingResponse> toPostingResponseList(List<LedgerPosting> postings);

    @Mapping(target = "totalDebit", ignore = true)
    @Mapping(target = "totalCredit", ignore = true)
    @Mapping(target = "balanced", ignore = true)
    @Mapping(target = "postings", source = "postings")
    JournalEntryResponse toJournalEntryResponse(JournalEntry journalEntry);

    @AfterMapping
    default void calculateTotals(JournalEntry journalEntry, @MappingTarget JournalEntryResponse response) {
        if (journalEntry.getPostings() != null) {
            BigDecimal totalDebit = journalEntry.getPostings().stream()
                    .filter(p -> p.getDirection() == PostingDirection.DEBIT)
                    .map(LedgerPosting::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCredit = journalEntry.getPostings().stream()
                    .filter(p -> p.getDirection() == PostingDirection.CREDIT)
                    .map(LedgerPosting::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            response.setTotalDebit(totalDebit);
            response.setTotalCredit(totalCredit);
            response.setBalanced(totalDebit.compareTo(totalCredit) == 0);
        }
    }
}
