package com.payflow.authservice.service;

import com.payflow.authservice.payload.responseDto.CloudinarySignatureResponse;
import com.payflow.authservice.payload.responseDto.CloudinaryUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    CloudinaryUploadResponse uploadAvatar(Long userId, MultipartFile file);

    CloudinarySignatureResponse generateSignedUploadParameters(Long userId);

    void deleteImage(String publicId);

    String getTransformedAvatarUrl(String publicId, int width, int height);
}

