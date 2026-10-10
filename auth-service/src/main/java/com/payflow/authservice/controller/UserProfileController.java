package com.payflow.authservice.controller;

import com.payflow.authservice.payload.requestDto.AvatarConfirmRequest;
import com.payflow.authservice.payload.requestDto.AvatarUploadUrlRequest;
import com.payflow.authservice.payload.requestDto.UpdateProfileRequest;
import com.payflow.authservice.payload.responseDto.AvatarDownloadUrlResponse;
import com.payflow.authservice.payload.responseDto.AvatarUploadUrlResponse;
import com.payflow.authservice.payload.responseDto.UserResponse;
import com.payflow.authservice.service.UserService;
import com.payflow.common.constant.Roles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    // get userprofile
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserProfile(@AuthenticationPrincipal Long userId) {
        UserResponse userProfileResponse = userService.getProfile(userId);
        return ResponseEntity.status(HttpStatus.OK).body(userProfileResponse);
    }

    // update profile
    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateUserProfile(@AuthenticationPrincipal Long userId,
                                                          @Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        UserResponse userProfileResponse = userService.updateProfile(userId, updateProfileRequest);
        return ResponseEntity.status(HttpStatus.OK).body(userProfileResponse);
    }

    @PostMapping("/roles/{role}")
    public ResponseEntity<UserResponse> addRole(
            @AuthenticationPrincipal Long userId,
            @PathVariable Roles role) {
        UserResponse response = userService.addRole(userId, role);
        return ResponseEntity.ok(response);
    }

    // 1. Generate Pre-signed Upload URL for Avatar
    @PostMapping("/avatar/upload-url")
    public ResponseEntity<AvatarUploadUrlResponse> generateAvatarUploadUrl(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AvatarUploadUrlRequest request) {
        AvatarUploadUrlResponse response = userService.generateAvatarUploadUrl(userId, request);
        return ResponseEntity.ok(response);
    }

    // 2. Confirm Avatar Upload (persists object key to user entity)
    @PostMapping("/avatar/confirm")
    public ResponseEntity<UserResponse> confirmAvatarUpload(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AvatarConfirmRequest request) {
        UserResponse response = userService.confirmAvatarUpload(userId, request);
        return ResponseEntity.ok(response);
    }

    // 3. Get Pre-signed Download/View URL for current user's avatar
    @GetMapping("/avatar/download-url")
    public ResponseEntity<AvatarDownloadUrlResponse> getMyAvatarDownloadUrl(
            @AuthenticationPrincipal Long userId) {
        AvatarDownloadUrlResponse response = userService.getAvatarDownloadUrl(userId);
        return ResponseEntity.ok(response);
    }

    // 4. Get Pre-signed Download/View URL for any user's avatar by ID
    @GetMapping("/{targetUserId}/avatar/download-url")
    public ResponseEntity<AvatarDownloadUrlResponse> getUserAvatarDownloadUrl(
            @PathVariable Long targetUserId) {
        AvatarDownloadUrlResponse response = userService.getAvatarDownloadUrlByUserId(targetUserId);
        return ResponseEntity.ok(response);
    }

}

