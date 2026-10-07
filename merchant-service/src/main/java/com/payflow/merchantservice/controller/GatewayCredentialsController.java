package com.payflow.merchantservice.controller;

import com.payflow.common.dto.ApiResponse;
import com.payflow.merchantservice.model.enums.GatewayProvider;
import com.payflow.merchantservice.payload.requestDto.AddGatewayCredentialsRequest;
import com.payflow.merchantservice.payload.responseDto.GatewayCredentialsResponse;
import com.payflow.merchantservice.service.GatewayCredentialsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/gateways")
@RequiredArgsConstructor
public class GatewayCredentialsController {

    private final GatewayCredentialsService gatewayCredentialsService;

    @PostMapping
    public ResponseEntity<ApiResponse<GatewayCredentialsResponse>> addOrUpdateCredentials(
            @PathVariable Long merchantId,
            @Valid @RequestBody AddGatewayCredentialsRequest request) {
        GatewayCredentialsResponse response = gatewayCredentialsService.addOrUpdateCredentials(merchantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<GatewayCredentialsResponse>builder()
                        .success(true)
                        .message("Gateway credentials saved successfully")
                        .data(response)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GatewayCredentialsResponse>>> getGatewayCredentials(
            @PathVariable Long merchantId) {
        List<GatewayCredentialsResponse> response = gatewayCredentialsService.getGatewayCredentials(merchantId);
        return ResponseEntity.ok(ApiResponse.<List<GatewayCredentialsResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/{gatewayType}")
    public ResponseEntity<ApiResponse<GatewayCredentialsResponse>> getGatewayCredentialsByType(
            @PathVariable Long merchantId,
            @PathVariable GatewayProvider gatewayType) {
        GatewayCredentialsResponse response = gatewayCredentialsService.getGatewayCredentialsByType(merchantId, gatewayType);
        return ResponseEntity.ok(ApiResponse.<GatewayCredentialsResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PatchMapping("/{gatewayType}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateGateway(
            @PathVariable Long merchantId,
            @PathVariable GatewayProvider gatewayType) {
        gatewayCredentialsService.deactivateGateway(merchantId, gatewayType);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Gateway deactivated successfully")
                .build());
    }

    @DeleteMapping("/{gatewayType}")
    public ResponseEntity<ApiResponse<Void>> deleteGateway(
            @PathVariable Long merchantId,
            @PathVariable GatewayProvider gatewayType) {
        gatewayCredentialsService.deleteGateway(merchantId, gatewayType);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Gateway credentials removed successfully")
                .build());
    }
}
