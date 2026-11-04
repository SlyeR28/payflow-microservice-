package com.payflow.merchantservice.service.impl;

import com.cloudinary.Cloudinary;
import com.payflow.merchantservice.config.CloudinaryProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecureStorageService {

    private final Cloudinary cloudinary;
    private final CloudinaryProperties cloudinaryProperties;

    /**
     * Generates a signed Cloudinary upload URL / endpoint for direct browser-to-Cloudinary upload.
     */
    public String generateUploadPresignedUrl(String objectKey, String contentType, Duration duration) {
        long timestamp = System.currentTimeMillis() / 1000L;
        String folder = "payflow/merchants/kyc";

        Map<String, Object> paramsToSign = new HashMap<>();
        paramsToSign.put("timestamp", timestamp);
        paramsToSign.put("folder", folder);
        paramsToSign.put("public_id", objectKey);

        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinaryProperties.getApiSecret());

        // Return signed upload endpoint with query parameters for direct REST PUT / POST upload
        String uploadUrl = String.format(
                "https://api.cloudinary.com/v1_1/%s/auto/upload?api_key=%s&timestamp=%d&signature=%s&folder=%s&public_id=%s",
                cloudinaryProperties.getCloudName(),
                cloudinaryProperties.getApiKey(),
                timestamp,
                signature,
                folder,
                objectKey
        );

        log.info("Generated signed Cloudinary upload URL for objectKey={}", objectKey);
        return uploadUrl;
    }

    /**
     * Generates a secure download/view URL for merchant KYC documents.
     */
    public String generateDownloadPresignedUrl(String objectKey, Duration duration) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        if (objectKey.startsWith("http://") || objectKey.startsWith("https://")) {
            return objectKey;
        }

        return cloudinary.url().secure(true).generate(objectKey);
    }
}
