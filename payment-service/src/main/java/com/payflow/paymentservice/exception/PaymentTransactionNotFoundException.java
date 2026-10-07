package com.payflow.paymentservice.exception;

public class PaymentTransactionNotFoundException extends RuntimeException {

    public PaymentTransactionNotFoundException(Long id) {
        super("Payment transaction not found with ID: " + id);
    }

    public PaymentTransactionNotFoundException(String reference) {
        super("Payment transaction not found with reference: " + reference);
    }
}
