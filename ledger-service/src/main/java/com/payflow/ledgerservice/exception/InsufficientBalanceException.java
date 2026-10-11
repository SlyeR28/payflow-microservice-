package com.payflow.ledgerservice.exception;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BaseException {

    public InsufficientBalanceException(String accountNumber, BigDecimal available, BigDecimal requested) {
        super(String.format("Insufficient available balance for account [%s]. Available: [%s], Requested: [%s]",
                accountNumber, available, requested), HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE");
    }
}
