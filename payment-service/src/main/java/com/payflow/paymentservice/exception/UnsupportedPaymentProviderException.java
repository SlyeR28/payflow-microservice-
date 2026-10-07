package com.payflow.paymentservice.exception;

import com.payflow.paymentservice.model.enums.PaymentProvider;

public class UnsupportedPaymentProviderException extends RuntimeException {

    public UnsupportedPaymentProviderException(PaymentProvider provider) {
        super("Unsupported payment provider: " + provider);
    }

    public UnsupportedPaymentProviderException(String message) {
        super(message);
    }
}
