package com.payflow.merchantservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidGatewayCredentialsException extends BaseException {

    public InvalidGatewayCredentialsException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_GATEWAY_CREDENTIALS");
    }
}
