package com.payflow.authservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyRegisteredException extends BaseException {
    public EmailAlreadyRegisteredException(String email) {
        super("Email Already Registered: " + email, HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED");
    }
}
