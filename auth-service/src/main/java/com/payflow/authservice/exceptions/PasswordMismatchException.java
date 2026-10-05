package com.payflow.authservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class PasswordMismatchException extends BaseException {
    public PasswordMismatchException() {
        super("Password and Confirm Password do not match", HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH");
    }
}
