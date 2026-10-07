package com.payflow.merchantservice.controller;

import com.payflow.common.dto.ApiResponse;
import com.payflow.merchantservice.payload.requestDto.AddressRequest;
import com.payflow.merchantservice.payload.responseDto.AddressResponse;
import com.payflow.merchantservice.service.MerchantAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/addresses")
@RequiredArgsConstructor
public class MerchantAddressController {

    private final MerchantAddressService merchantAddressService;

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @PathVariable Long merchantId,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = merchantAddressService.addAddress(merchantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AddressResponse>builder()
                        .success(true)
                        .message("Address added successfully")
                        .data(response)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(@PathVariable Long merchantId) {
        List<AddressResponse> response = merchantAddressService.getMerchantAddresses(merchantId);
        return ResponseEntity.ok(ApiResponse.<List<AddressResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @PathVariable Long merchantId,
            @PathVariable Long addressId) {
        AddressResponse response = merchantAddressService.getAddressById(addressId);
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long merchantId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = merchantAddressService.updateAddress(addressId, request);
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .success(true)
                .message("Address updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long merchantId,
            @PathVariable Long addressId) {
        merchantAddressService.deleteAddress(addressId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Address deleted successfully")
                .build());
    }
}
