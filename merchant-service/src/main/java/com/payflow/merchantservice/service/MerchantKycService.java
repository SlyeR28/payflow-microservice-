package com.payflow.merchantservice.service;

import com.payflow.merchantservice.payload.requestDto.KycConfirmRequest;
import com.payflow.merchantservice.payload.requestDto.KycUploadUrlRequest;
import com.payflow.merchantservice.payload.requestDto.VerifyKycRequest;
import com.payflow.merchantservice.payload.responseDto.KycDocumentResponse;
import com.payflow.merchantservice.payload.responseDto.KycStatusResponse;
import com.payflow.merchantservice.payload.responseDto.MerchantResponse;
import com.payflow.merchantservice.payload.responseDto.UploadUrlResponse;

import java.util.List;

public interface MerchantKycService {

    UploadUrlResponse generateUploadUrl(Long merchantId, KycUploadUrlRequest request);

    KycDocumentResponse confirmDocumentUpload(Long merchantId, KycConfirmRequest request);

    KycDocumentResponse verifyDocument(Long merchantId, VerifyKycRequest request);

    KycStatusResponse getKycStatus(Long merchantId);

    List<KycDocumentResponse> getMerchantDocuments(Long merchantId);

    MerchantResponse submitKycForReview(Long merchantId);
}
