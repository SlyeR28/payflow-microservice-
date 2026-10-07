package com.payflow.merchantservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class BankAccountNotFoundException extends BaseException {

    public BankAccountNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "BANK_ACCOUNT_NOT_FOUND");
    }

    public BankAccountNotFoundException(Long accountId) {
        super("Bank account not found with id: " + accountId, HttpStatus.NOT_FOUND, "BANK_ACCOUNT_NOT_FOUND");
    }
}
