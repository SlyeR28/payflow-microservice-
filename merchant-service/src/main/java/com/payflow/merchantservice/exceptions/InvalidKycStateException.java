package com.payflow.merchantservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidKycStateException extends BaseException {

    public InvalidKycStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_KYC_STATE");
    }
}
