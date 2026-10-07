package com.payflow.merchantservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class GatewayCredentialsNotFoundException extends BaseException {

    public GatewayCredentialsNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "GATEWAY_CREDENTIALS_NOT_FOUND");
    }

    public GatewayCredentialsNotFoundException(Long configId) {
        super("Gateway credentials not found with id: " + configId, HttpStatus.NOT_FOUND, "GATEWAY_CREDENTIALS_NOT_FOUND");
    }
}
