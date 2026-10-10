package com.payflow.merchantservice.controller;

import com.payflow.common.dto.ApiResponse;
import com.payflow.common.dto.PagedResponse;
import com.payflow.common.util.PaginationUtil;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import com.payflow.merchantservice.payload.requestDto.CreateMerchantRequest;
import com.payflow.merchantservice.payload.requestDto.UpdateMerchantRequest;
import com.payflow.merchantservice.payload.responseDto.MerchantResponse;
import com.payflow.merchantservice.security.service.SecurityUtil;
import com.payflow.merchantservice.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;
    private final SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<MerchantResponse>> createMerchant(
            @Valid @RequestBody CreateMerchantRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        MerchantResponse response = merchantService.createMerchant(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<MerchantResponse>builder()
                        .success(true)
                        .message("Merchant profile created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MerchantResponse>> getMyProfile() {
        Long userId = securityUtil.getCurrentUserId();
        MerchantResponse response = merchantService.getMerchantByUserId(userId);
        return ResponseEntity.ok(ApiResponse.<MerchantResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PutMapping("/{merchantId}")
    public ResponseEntity<ApiResponse<MerchantResponse>> updateMerchant(
             @PathVariable Long merchantId,
            @Valid @RequestBody UpdateMerchantRequest request) {
        MerchantResponse response = merchantService.updateMerchant(merchantId, request);
        return ResponseEntity.ok(ApiResponse.<MerchantResponse>builder()
                .success(true)
                .message("Merchant profile updated successfully")
                .data(response)
                .build());
    }

    @PostMapping("/{id}/verify-pan")
    public ResponseEntity<ApiResponse<MerchantResponse>> verifyPan(@PathVariable Long id) {
        MerchantResponse response = merchantService.verifyPan(id);
        return ResponseEntity.ok(ApiResponse.<MerchantResponse>builder()
                .success(true)
                .message("Merchant PAN verified successfully")
                .data(response)
                .build());
    }
}
