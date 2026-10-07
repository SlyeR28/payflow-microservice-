package com.payflow.paymentservice.service;

import com.payflow.paymentservice.dto.*;
import com.payflow.paymentservice.model.enums.PaymentProvider;

import java.util.List;

public interface PaymentService {

    GatewayVerificationResponse verifyCredentials(VerifyCredentialsRequest request);

    PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request);

    PaymentInitiateResponse getTransactionById(Long id);

    PaymentInitiateResponse getTransactionByReference(String reference);

    RefundResponse refundPayment(String reference, RefundInitiateRequest request);

    List<PaymentProvider> getSupportedProviders();
}
