package com.payflow.merchantservice.service;

import com.payflow.common.dto.PagedResponse;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import com.payflow.merchantservice.payload.requestDto.CreateMerchantRequest;
import com.payflow.merchantservice.payload.requestDto.UpdateMerchantRequest;
import com.payflow.merchantservice.payload.responseDto.MerchantResponse;
import org.springframework.data.domain.Pageable;

public interface MerchantService {

    // user (uses userId from jwt)

    MerchantResponse createMerchant(Long userId, CreateMerchantRequest request);

    MerchantResponse getMerchantByUserId(Long userId);

    MerchantResponse updateMerchant(Long merchantId, UpdateMerchantRequest request);

    MerchantResponse verifyPan(Long merchantId);


    // Amin(uses merchantId)
    MerchantResponse getMerchantById(Long merchantId);

    PagedResponse<MerchantResponse>listAllByStatus(MerchantStatus status , Pageable pageable);

    MerchantResponse approve(Long merchantId, Long adminId);

    MerchantResponse reject(Long merchantId, Long adminId , String reason);

    MerchantResponse suspend(Long merchantId , Long adminId);

    PagedResponse<MerchantResponse> getAllMerchants(Pageable pageable);

}
