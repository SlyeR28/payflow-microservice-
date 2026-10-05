package com.payflow.authservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException() {
        super("Invalid username/email or password", HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS");
    }
}
