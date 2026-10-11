package com.payflow.ledgerservice.exception;

import com.payflow.common.exceptions.BaseException;
import com.payflow.ledgerservice.model.enums.AccountStatus;
import org.springframework.http.HttpStatus;

public class AccountInactiveException extends BaseException {

    public AccountInactiveException(String accountNumber, AccountStatus status) {
        super(String.format("Account [%s] is not active. Current status: [%s]", accountNumber, status),
                HttpStatus.BAD_REQUEST, "ACCOUNT_INACTIVE");
    }
}
