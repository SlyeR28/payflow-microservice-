package com.payflow.authservice.service;

import com.payflow.authservice.payload.requestDto.AvatarConfirmRequest;
import com.payflow.authservice.payload.requestDto.AvatarUploadUrlRequest;
import com.payflow.authservice.payload.requestDto.UpdateProfileRequest;
import com.payflow.authservice.payload.responseDto.AvatarDownloadUrlResponse;
import com.payflow.authservice.payload.responseDto.AvatarUploadUrlResponse;
import com.payflow.authservice.payload.responseDto.CloudinarySignatureResponse;
import com.payflow.authservice.payload.responseDto.UserResponse;
import com.payflow.common.constant.Roles;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserResponse getProfile(Long userId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    UserResponse addRole(Long userId, Roles roles);

    AvatarUploadUrlResponse generateAvatarUploadUrl(Long userId, AvatarUploadUrlRequest request);
    UserResponse confirmAvatarUpload(Long userId, AvatarConfirmRequest request);
    AvatarDownloadUrlResponse getAvatarDownloadUrl(Long userId);
    AvatarDownloadUrlResponse getAvatarDownloadUrlByUserId(Long targetUserId);

    // Cloudinary Image Handling
    UserResponse uploadAvatarCloudinary(Long userId, MultipartFile file);
    CloudinarySignatureResponse getCloudinaryUploadSignature(Long userId);
}
