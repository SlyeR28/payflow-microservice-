package com.payflow.authservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidOtpException extends BaseException {

    public InvalidOtpException() {
        super("OTP is Invalid or has expired", HttpStatus.BAD_REQUEST, "INVALID_OTP");
    }
}
