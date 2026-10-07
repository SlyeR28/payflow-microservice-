package com.payflow.merchantservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class MerchantAlreadyExistsException extends BaseException {

    public MerchantAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, "MERCHANT_ALREADY_EXISTS");
    }

    public MerchantAlreadyExistsException(Long userId) {
        super("Merchant profile already exists for user id: " + userId, HttpStatus.CONFLICT, "MERCHANT_ALREADY_EXISTS");
    }
}
