package com.payflow.merchantservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class MerchantNotFoundException extends BaseException {

    public MerchantNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "MERCHANT_NOT_FOUND");
    }

    public MerchantNotFoundException(Long merchantId) {
        super("Merchant not found with id: " + merchantId, HttpStatus.NOT_FOUND, "MERCHANT_NOT_FOUND");
    }
}
