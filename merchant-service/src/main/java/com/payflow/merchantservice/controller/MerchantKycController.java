package com.payflow.merchantservice.controller;

import com.payflow.common.dto.ApiResponse;
import com.payflow.merchantservice.payload.requestDto.KycConfirmRequest;
import com.payflow.merchantservice.payload.requestDto.KycUploadUrlRequest;
import com.payflow.merchantservice.payload.requestDto.VerifyKycRequest;
import com.payflow.merchantservice.payload.responseDto.*;
import com.payflow.merchantservice.service.MerchantKycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/kyc")
@RequiredArgsConstructor
public class MerchantKycController {

    private final MerchantKycService merchantKycService;

    @PostMapping("/documents/upload-url")
    public ResponseEntity<ApiResponse<UploadUrlResponse>> getUploadUrl(
            @PathVariable Long merchantId,
            @Valid @RequestBody KycUploadUrlRequest request) {
        UploadUrlResponse response = merchantKycService.generateUploadUrl(merchantId, request);
        return ResponseEntity.ok(ApiResponse.<UploadUrlResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PostMapping("/documents/confirm")
    public ResponseEntity<ApiResponse<KycDocumentResponse>> confirmDocument(
            @PathVariable Long merchantId,
            @Valid @RequestBody KycConfirmRequest request) {
        KycDocumentResponse response = merchantKycService.confirmDocumentUpload(merchantId, request);
        return ResponseEntity.ok(ApiResponse.<KycDocumentResponse>builder()
                .success(true)
                .message("Document upload confirmed successfully")
                .data(response)
                .build());
    }

    @PostMapping("/documents/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<KycDocumentResponse>> verifyDocument(
            @PathVariable Long merchantId,
            @Valid @RequestBody VerifyKycRequest request) {
        KycDocumentResponse response = merchantKycService.verifyDocument(merchantId, request);
        return ResponseEntity.ok(ApiResponse.<KycDocumentResponse>builder()
                .success(true)
                .message("Document verification status updated")
                .data(response)
                .build());
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<KycStatusResponse>> getKycStatus(@PathVariable Long merchantId) {
        KycStatusResponse response = merchantKycService.getKycStatus(merchantId);
        return ResponseEntity.ok(ApiResponse.<KycStatusResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>> getDocuments(@PathVariable Long merchantId) {
        List<KycDocumentResponse> response = merchantKycService.getMerchantDocuments(merchantId);
        return ResponseEntity.ok(ApiResponse.<List<KycDocumentResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<MerchantResponse>> submitForReview(@PathVariable Long merchantId) {
        MerchantResponse response = merchantKycService.submitKycForReview(merchantId);
        return ResponseEntity.ok(ApiResponse.<MerchantResponse>builder()
                .success(true)
                .message("KYC submitted for review successfully")
                .data(response)
                .build());
    }

    @GetMapping("/documents/{documentId}/download-url")
    public ResponseEntity<ApiResponse<DownloadUrlResponse>> getDownloadUrl(
            @PathVariable Long merchantId,
            @PathVariable Long documentId
    ){
        DownloadUrlResponse response = merchantKycService.generateDownloadUrl(merchantId, documentId);
        return ResponseEntity.ok(ApiResponse.<DownloadUrlResponse>builder()
                .success(true)
                .data(response)
                .build());
    }
}
