package com.payflow.ledgerservice.exception;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

public class UnbalancedJournalEntryException extends BaseException {

    public UnbalancedJournalEntryException(BigDecimal totalDebit, BigDecimal totalCredit) {
        super(String.format("Journal entry is not balanced. Total Debits [%s] != Total Credits [%s]",
                totalDebit, totalCredit), HttpStatus.BAD_REQUEST, "UNBALANCED_JOURNAL_ENTRY");
    }

    public UnbalancedJournalEntryException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "UNBALANCED_JOURNAL_ENTRY");
    }
}
