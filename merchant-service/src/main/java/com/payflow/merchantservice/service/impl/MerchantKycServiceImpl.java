package com.payflow.merchantservice.service.impl;

import com.payflow.merchantservice.exceptions.InvalidKycStateException;
import com.payflow.merchantservice.exceptions.InvalidMerchantStateException;
import com.payflow.merchantservice.exceptions.KycDocumentNotFoundException;
import com.payflow.merchantservice.exceptions.MerchantNotFoundException;
import com.payflow.merchantservice.mapper.MerchantAddressMapper;
import com.payflow.merchantservice.mapper.MerchantKycMapper;
import com.payflow.merchantservice.mapper.MerchantMapper;
import com.payflow.merchantservice.model.entity.Merchant;
import com.payflow.merchantservice.model.entity.MerchantAddress;
import com.payflow.merchantservice.model.entity.MerchantDocument;
import com.payflow.merchantservice.model.entity.MerchantKyc;
import com.payflow.merchantservice.model.enums.DocumentType;
import com.payflow.merchantservice.model.enums.KycStatus;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import com.payflow.merchantservice.payload.requestDto.KycConfirmRequest;
import com.payflow.merchantservice.payload.requestDto.KycUploadUrlRequest;
import com.payflow.merchantservice.payload.requestDto.VerifyKycRequest;
import com.payflow.merchantservice.payload.responseDto.AddressResponse;
import com.payflow.merchantservice.payload.responseDto.KycDocumentResponse;
import com.payflow.merchantservice.payload.responseDto.KycStatusResponse;
import com.payflow.merchantservice.payload.responseDto.MerchantResponse;
import com.payflow.merchantservice.payload.responseDto.UploadUrlResponse;
import com.payflow.merchantservice.repository.MerchantAddressRepository;
import com.payflow.merchantservice.repository.MerchantDocumentRepository;
import com.payflow.merchantservice.repository.MerchantKycRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import com.payflow.merchantservice.service.MerchantKycService;
import com.payflow.merchantservice.utils.MaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MerchantKycServiceImpl implements MerchantKycService {

    private final MerchantRepository merchantRepository;
    private final MerchantKycRepository merchantKycRepository;
    private final MerchantDocumentRepository merchantDocumentRepository;
    private final MerchantAddressRepository merchantAddressRepository;
    private final MerchantKycMapper merchantKycMapper;
    private final MerchantMapper merchantMapper;
    private final MerchantAddressMapper merchantAddressMapper;
    private final SecureStorageService secureStorageService;

    @Override
    public UploadUrlResponse generateUploadUrl(Long merchantId, KycUploadUrlRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() ->
                new MerchantNotFoundException(merchantId));

        if (merchant.getStatus() == MerchantStatus.SUSPENDED || merchant.getStatus() == MerchantStatus.REJECTED) {
            throw new InvalidMerchantStateException("Merchant in status " + merchant.getStatus() + " cannot upload KYC documents");
        }

        String originalName = request.getFileName();
        String fileExt = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                : "";

        String s3Key = String.format("merchants/%d/kyc/%s_%s%s",
                merchantId, request.getDocumentType().name().toLowerCase(), UUID.randomUUID(), fileExt);

        String uploadUrl = secureStorageService.generateUploadPresignedUrl(s3Key, request.getContentType(), Duration.ofMinutes(15));

        log.info("Generated pre-signed upload URL for merchantId={}, docType={}, s3Key={}",
                merchantId, request.getDocumentType(), s3Key);

        return UploadUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .s3Key(s3Key)
                .expiresInMinutes(15)
                .build();
    }

    @Override
    public KycDocumentResponse confirmDocumentUpload(Long merchantId, KycConfirmRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() ->
                new MerchantNotFoundException(merchantId));

        MerchantKyc kyc = merchantKycRepository
                .findByMerchantIdAndDocumentType(merchantId, request.getDocumentType())
                .orElse(null);

        if (kyc == null) {
            kyc = MerchantKyc.builder()
                    .merchant(merchant)
                    .documentType(request.getDocumentType())
                    .status(KycStatus.SUBMITTED)
                    .build();
        } else {
            kyc.setStatus(KycStatus.SUBMITTED);
            kyc.setRejectionReason(null);
        }

        kyc.setS3ObjectKey(request.getS3ObjectKey());
        if (request.getDocumentNumber() != null && !request.getDocumentNumber().isBlank()) {
            kyc.setDocumentNumberMasked(MaskingUtil.maskPan(request.getDocumentNumber()));
        }
        MerchantKyc savedKyc = merchantKycRepository.save(kyc);

        merchantDocumentRepository.save(MerchantDocument.builder()
                .merchant(merchant)
                .documentName(request.getDocumentType().name())
                .s3ObjectKey(request.getS3ObjectKey())
                .build());

        log.info("Confirmed document upload for merchantId={}, docType={}, kycId={}",
                merchantId, request.getDocumentType(), savedKyc.getId());

        return merchantKycMapper.toResponse(savedKyc);
    }

    @Override
    public KycDocumentResponse verifyDocument(Long merchantId, VerifyKycRequest request) {
        MerchantKyc kyc = merchantKycRepository.findById(request.getDocumentId()).orElseThrow(() ->
                new KycDocumentNotFoundException(request.getDocumentId()));

        if (!kyc.getMerchant().getId().equals(merchantId)) {
            throw new KycDocumentNotFoundException("Document with id " + request.getDocumentId() + " does not belong to merchant " + merchantId);
        }

        if (Boolean.TRUE.equals(request.getIsApproved())) {
            kyc.setStatus(KycStatus.VERIFIED);
            kyc.setVerifiedAt(Instant.now());
            kyc.setRejectionReason(null);
            log.info("KYC document verified: docId={}, merchantId={}", request.getDocumentId(), merchantId);
        } else {
            kyc.setStatus(KycStatus.REJECTED);
            kyc.setRejectionReason(request.getRejectionReason() != null && !request.getRejectionReason().isBlank()
                    ? request.getRejectionReason()
                    : "Document rejected during compliance review");
            log.warn("KYC document rejected: docId={}, merchantId={}, reason={}",
                    request.getDocumentId(), merchantId, kyc.getRejectionReason());
        }

        MerchantKyc savedKyc = merchantKycRepository.save(kyc);
        return merchantKycMapper.toResponse(savedKyc);
    }

    @Override
    @Transactional(readOnly = true)
    public KycStatusResponse getKycStatus(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() ->
                new MerchantNotFoundException(merchantId));

        List<MerchantKyc> docs = merchantKycRepository.findByMerchantId(merchantId);
        long verified = docs.stream().filter(d -> d.getStatus() == KycStatus.VERIFIED).count();
        long rejected = docs.stream().filter(d -> d.getStatus() == KycStatus.REJECTED).count();
        long pending = docs.stream().filter(d -> d.getStatus() == KycStatus.PENDING
                || d.getStatus() == KycStatus.SUBMITTED
                || d.getStatus() == KycStatus.UNDER_REVIEW).count();

        boolean allVerified = !docs.isEmpty() && verified == docs.size();

        return KycStatusResponse.builder()
                .merchantId(merchantId)
                .merchantStatus(merchant.getStatus())
                .totalDocuments(docs.size())
                .verifiedDocuments(verified)
                .pendingDocuments(pending)
                .rejectedDocuments(rejected)
                .allVerified(allVerified)
                .documents(merchantKycMapper.toResponseList(docs))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getMerchantDocuments(Long merchantId) {
        merchantRepository.findById(merchantId).orElseThrow(() ->
                new MerchantNotFoundException(merchantId));
        return merchantKycMapper.toResponseList(merchantKycRepository.findByMerchantId(merchantId));
    }

    @Override
    public MerchantResponse submitKycForReview(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() ->
                new MerchantNotFoundException(merchantId));

        if (merchant.getStatus() == MerchantStatus.SUSPENDED) {
            throw new InvalidMerchantStateException("Suspended merchant cannot submit KYC for review");
        }
        if (merchant.getStatus() == MerchantStatus.ACTIVE) {
            throw new InvalidMerchantStateException("Merchant is already active");
        }

        // 1. Verify PAN is manually present on the profile (No PAN image upload required)
        if (merchant.getPanNumberEncrypted() == null || merchant.getPanNumberEncrypted().isBlank()) {
            throw new InvalidKycStateException("Merchant PAN number is missing. Please provide PAN on profile before submitting KYC.");
        }

        // 2. Validate mandatory uploaded documents (Cancelled Cheque / Bank proof)
        List<MerchantKyc> docs = merchantKycRepository.findByMerchantId(merchantId);
        if (docs.isEmpty()) {
            throw new InvalidKycStateException("Please upload required settlement bank documents (Cancelled Cheque) before submitting KYC.");
        }

        boolean hasBankProof = docs.stream().anyMatch(d -> d.getDocumentType() == DocumentType.CANCELLED_CHEQUE);
        if (!hasBankProof) {
            throw new InvalidKycStateException("Cancelled Cheque is mandatory for settlement bank verification.");
        }

        // 3. Transition all submitted documents to UNDER_REVIEW
        for (MerchantKyc doc : docs) {
            if (doc.getStatus() == KycStatus.SUBMITTED) {
                doc.setStatus(KycStatus.UNDER_REVIEW);
            }
        }
        merchantKycRepository.saveAll(docs);

        // 4. Transition Merchant to UNDER_REVIEW
        merchant.setStatus(MerchantStatus.UNDER_REVIEW);
        merchant.setRejectReason(null);
        Merchant savedMerchant = merchantRepository.save(merchant);

        log.info("KYC submitted for review: merchantId={}, documentsCount={}", merchantId, docs.size());

        return toMerchantResponseWithAddresses(savedMerchant);
    }

    private MerchantResponse toMerchantResponseWithAddresses(Merchant merchant) {
        MerchantResponse response = merchantMapper.toResponse(merchant);
        List<MerchantAddress> merchantAddresses = merchantAddressRepository.findByMerchantId(merchant.getId());
        if (merchantAddresses.isEmpty() && merchant.getAddresses() != null && !merchant.getAddresses().isEmpty()) {
            merchantAddresses = merchant.getAddresses();
        }
        List<AddressResponse> addresses = merchantAddresses.stream()
                .map(merchantAddressMapper::toResponse)
                .toList();
        response.setAddresses(addresses);
        return response;
    }
}


