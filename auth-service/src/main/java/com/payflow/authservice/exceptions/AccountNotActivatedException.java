package com.payflow.authservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class AccountNotActivatedException extends BaseException {

    public AccountNotActivatedException(String reason) {
        super(reason, HttpStatus.FORBIDDEN, "ACCOUNT_NOT_ACTIVATED");
    }
}
