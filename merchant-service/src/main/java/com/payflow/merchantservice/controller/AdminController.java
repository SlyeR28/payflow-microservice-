package com.payflow.merchantservice.controller;

import com.payflow.common.dto.ApiResponse;
import com.payflow.common.dto.PagedResponse;
import com.payflow.common.util.PaginationUtil;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import com.payflow.merchantservice.payload.requestDto.RejectMerchantRequest;
import com.payflow.merchantservice.payload.responseDto.MerchantResponse;
import com.payflow.merchantservice.security.service.SecurityUtil;
import com.payflow.merchantservice.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/admin/merchants", "/api/v1/merchants"})
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final MerchantService merchantService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<MerchantResponse>>> getAllMerchants(
            @RequestParam(required = false) MerchantStatus status,
            @RequestParam(defaultValue = "" + PaginationUtil.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = "" + PaginationUtil.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = PaginationUtil.DEFAULT_SORT_DIRECTION) String sortDir) {
        Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, sortDir);
        PagedResponse<MerchantResponse> response = (status != null)
                ? merchantService.listAllByStatus(status, pageable)
                : merchantService.getAllMerchants(pageable);
        return ResponseEntity.ok(ApiResponse.<PagedResponse<MerchantResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PagedResponse<MerchantResponse>>> getMerchantsByStatus(
            @PathVariable MerchantStatus status,
            @RequestParam(defaultValue = "" + PaginationUtil.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = "" + PaginationUtil.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = PaginationUtil.DEFAULT_SORT_DIRECTION) String sortDir) {
        Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, sortDir);
        PagedResponse<MerchantResponse> response = merchantService.listAllByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.<PagedResponse<MerchantResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MerchantResponse>> getMerchantById(@PathVariable Long id) {
        MerchantResponse response = merchantService.getMerchantById(id);
        return ResponseEntity.ok(ApiResponse.<MerchantResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @RequestMapping(value = "/{id}/approve", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<MerchantResponse>> approveMerchant(@PathVariable Long id) {
        Long adminId = securityUtil.getCurrentUserId();
        MerchantResponse response = merchantService.approve(id, adminId);
        return ResponseEntity.ok(ApiResponse.<MerchantResponse>builder()
                .success(true)
                .message("Merchant approved successfully")
                .data(response)
                .build());
    }

    @RequestMapping(value = "/{id}/reject", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<MerchantResponse>> rejectMerchant(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) RejectMerchantRequest request,
            @RequestParam(required = false) String reason) {
        Long adminId = securityUtil.getCurrentUserId();
        String effectiveReason = (request != null && request.getReason() != null && !request.getReason().isBlank())
                ? request.getReason()
                : reason;
        if (effectiveReason == null || effectiveReason.isBlank()) {
            effectiveReason = "Rejected by administrator";
        }
        MerchantResponse response = merchantService.reject(id, adminId, effectiveReason);
        return ResponseEntity.ok(ApiResponse.<MerchantResponse>builder()
                .success(true)
                .message("Merchant rejected successfully")
                .data(response)
                .build());
    }

    @RequestMapping(value = "/{id}/suspend", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<MerchantResponse>> suspendMerchant(@PathVariable Long id) {
        Long adminId = securityUtil.getCurrentUserId();
        MerchantResponse response = merchantService.suspend(id, adminId);
        return ResponseEntity.ok(ApiResponse.<MerchantResponse>builder()
                .success(true)
                .message("Merchant suspended successfully")
                .data(response)
                .build());
    }
}
