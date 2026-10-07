package com.payflow.merchantservice.service.impl;

import com.payflow.merchantservice.mapper.MerchantKycMapper;
import com.payflow.merchantservice.payload.requestDto.KycConfirmRequest;
import com.payflow.merchantservice.payload.requestDto.KycUploadUrlRequest;
import com.payflow.merchantservice.payload.requestDto.VerifyKycRequest;
import com.payflow.merchantservice.payload.responseDto.KycDocumentResponse;
import com.payflow.merchantservice.payload.responseDto.KycStatusResponse;
import com.payflow.merchantservice.payload.responseDto.MerchantResponse;
import com.payflow.merchantservice.payload.responseDto.UploadUrlResponse;
import com.payflow.merchantservice.repository.MerchantKycRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import com.payflow.merchantservice.service.MerchantKycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MerchantKycServiceImpl implements MerchantKycService {

    private final MerchantRepository merchantRepository;
    private final MerchantKycRepository merchantKycRepository;
    private final MerchantKycMapper merchantKycMapper;

    @Override
    public UploadUrlResponse generateUploadUrl(Long merchantId, KycUploadUrlRequest request) {
        // TODO: Implement logic (generate S3 pre-signed PUT URL with 15 mins expiry)
        return null;
    }

    @Override
    public KycDocumentResponse confirmDocumentUpload(Long merchantId, KycConfirmRequest request) {
        // TODO: Implement logic (record document with status SUBMITTED, mask doc number)
        return null;
    }

    @Override
    public KycDocumentResponse verifyDocument(Long merchantId, VerifyKycRequest request) {
        // TODO: Implement logic (approve/reject document, update status and reason)
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public KycStatusResponse getKycStatus(Long merchantId) {
        // TODO: Implement logic (aggregate counts of documents and verification status)
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getMerchantDocuments(Long merchantId) {
        // TODO: Implement logic (fetch all uploaded documents for merchant)
        return List.of();
    }

    @Override
    public MerchantResponse submitKycForReview(Long merchantId) {
        // TODO: Implement logic (validate mandatory documents present, transition status to UNDER_REVIEW)
        return null;
    }
}
