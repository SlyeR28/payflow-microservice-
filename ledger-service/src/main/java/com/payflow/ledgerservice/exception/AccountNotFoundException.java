package com.payflow.ledgerservice.exception;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class AccountNotFoundException extends BaseException {

    public AccountNotFoundException(String accountNumber) {
        super(String.format("Account with number [%s] was not found", accountNumber),
                HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND");
    }

    public AccountNotFoundException(Long id) {
        super(String.format("Account with ID [%d] was not found", id),
                HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND");
    }
}
