package com.payflow.merchantservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class PrimaryBankAccountCannotBeDeletedException extends BaseException {

    public PrimaryBankAccountCannotBeDeletedException() {
        super("Primary bank account cannot be deleted without setting another account as primary",
                HttpStatus.BAD_REQUEST, "PRIMARY_BANK_ACCOUNT_DELETE_FORBIDDEN");
    }
}
