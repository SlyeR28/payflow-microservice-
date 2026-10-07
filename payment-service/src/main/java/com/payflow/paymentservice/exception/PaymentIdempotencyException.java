package com.payflow.paymentservice.exception;

public class PaymentIdempotencyException extends RuntimeException {

    public PaymentIdempotencyException(String message) {
        super(message);
    }
}
