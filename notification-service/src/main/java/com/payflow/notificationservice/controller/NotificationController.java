package com.payflow.notificationservice.controller;

import com.payflow.common.dto.ApiResponse;
import com.payflow.notificationservice.dto.request.CreateTemplateRequest;
import com.payflow.notificationservice.dto.request.SendNotificationRequest;
import com.payflow.notificationservice.dto.request.UpdatePreferenceRequest;
import com.payflow.notificationservice.dto.response.NotificationResponse;
import com.payflow.notificationservice.dto.response.PreferenceResponse;
import com.payflow.notificationservice.dto.response.TemplateResponse;
import com.payflow.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        log.info("Received request to send [{}] notification to [{}]", request.getChannel(), request.getRecipient());
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<NotificationResponse>builder()
                        .success(true)
                        .message("Notification dispatched successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(
            @PathVariable String reference) {
        NotificationResponse response = notificationService.getNotificationByReference(reference);
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification retrieved successfully")
                .data(response)
                .build());
    }

    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<TemplateResponse>> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request) {
        TemplateResponse response = notificationService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<TemplateResponse>builder()
                        .success(true)
                        .message("Template created successfully")
                        .data(response)
                        .build());
    }

    @PutMapping("/preferences/{merchantId}")
    public ResponseEntity<ApiResponse<PreferenceResponse>> updatePreference(
            @PathVariable Long merchantId,
            @Valid @RequestBody UpdatePreferenceRequest request) {
        PreferenceResponse response = notificationService.updatePreference(merchantId, request);
        return ResponseEntity.ok(ApiResponse.<PreferenceResponse>builder()
                .success(true)
                .message("Notification preferences updated successfully")
                .data(response)
                .build());
    }

    @GetMapping("/preferences/{merchantId}")
    public ResponseEntity<ApiResponse<PreferenceResponse>> getPreference(
            @PathVariable Long merchantId) {
        PreferenceResponse response = notificationService.getPreference(merchantId);
        return ResponseEntity.ok(ApiResponse.<PreferenceResponse>builder()
                .success(true)
                .message("Notification preferences retrieved successfully")
                .data(response)
                .build());
    }
}
