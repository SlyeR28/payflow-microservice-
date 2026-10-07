package com.payflow.paymentservice.controller;

import com.payflow.common.dto.ApiResponse;
import com.payflow.paymentservice.dto.*;
import com.payflow.paymentservice.model.enums.PaymentProvider;
import com.payflow.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Granular pre-flight verification against live gateway sandbox APIs using Spring RestClient.
     */
    @PostMapping("/verify-credentials")
    public ResponseEntity<ApiResponse<GatewayVerificationResponse>> verifyCredentials(
            @Valid @RequestBody VerifyCredentialsRequest request) {
        log.info("REST: verify-credentials for provider: {}", request.getProvider());
        GatewayVerificationResponse response = paymentService.verifyCredentials(request);
        return ResponseEntity.ok(ApiResponse.success(
                response.isValid() ? "Gateway credentials verified successfully" : "Gateway credentials verification failed",
                response
        ));
    }

    /**
     * Initiate payment transaction and obtain provider checkout tokens.
     */
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentInitiateResponse>> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request) {
        log.info("REST: initiatePayment with provider: {}, amount: {}", request.getProvider(), request.getAmount());
        PaymentInitiateResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initiated successfully", response));
    }

    /**
     * Fetch payment transaction by internal ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentInitiateResponse>> getTransactionById(
            @PathVariable Long id) {
        PaymentInitiateResponse response = paymentService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Fetch payment transaction by unique transaction reference.
     */
    @GetMapping("/ref/{reference}")
    public ResponseEntity<ApiResponse<PaymentInitiateResponse>> getTransactionByReference(
            @PathVariable String reference) {
        PaymentInitiateResponse response = paymentService.getTransactionByReference(reference);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Refund payment transaction.
     */
    @PostMapping("/{reference}/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> refundPayment(
            @PathVariable String reference,
            @RequestBody(required = false) RefundInitiateRequest request) {
        log.info("REST: refundPayment for reference: {}", reference);
        RefundResponse response = paymentService.refundPayment(reference, request);
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", response));
    }

    /**
     * List all supported payment providers.
     */
    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<PaymentProvider>>> getSupportedProviders() {
        List<PaymentProvider> providers = paymentService.getSupportedProviders();
        return ResponseEntity.ok(ApiResponse.success("Supported payment providers", providers));
    }
}
