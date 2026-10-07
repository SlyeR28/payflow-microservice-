package com.payflow.merchantservice.exceptions;

import com.payflow.common.exceptions.BaseException;

public class InvalidMerchantStateException extends BaseException {
    public InvalidMerchantStateException(String message) {
        super(message);
    }
}
