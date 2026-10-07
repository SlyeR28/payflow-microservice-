package com.payflow.paymentservice.strategy;

import com.payflow.paymentservice.dto.GatewayCredentials;
import com.payflow.paymentservice.dto.GatewayPaymentResult;
import com.payflow.paymentservice.dto.GatewayRefundResult;
import com.payflow.paymentservice.dto.GatewayVerificationResponse;
import com.payflow.paymentservice.model.entity.PaymentTransaction;
import com.payflow.paymentservice.model.enums.PaymentProvider;

import java.math.BigDecimal;

/**
 * Strategy interface defining contracts for all payment gateway integrations.
 */
public interface PaymentGatewayStrategy {

    /**
     * Identifies the provider for this strategy.
     */
    PaymentProvider getProvider();

    /**
     * Granular pre-flight verification against official provider sandbox API
     * using Spring RestClient to check whether keys are real, active, and authorized.
     */
    GatewayVerificationResponse verifyCredentials(GatewayCredentials credentials);

    /**
     * Initiates payment order / intent with the external gateway.
     */
    GatewayPaymentResult initiatePayment(PaymentTransaction transaction, GatewayCredentials credentials);

    /**
     * Issues a refund against an existing captured transaction.
     */
    GatewayRefundResult refundPayment(PaymentTransaction transaction, BigDecimal refundAmount, String reason, GatewayCredentials credentials);
}
