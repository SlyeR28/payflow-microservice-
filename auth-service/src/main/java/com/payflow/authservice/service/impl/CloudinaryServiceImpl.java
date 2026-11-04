package com.payflow.authservice.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.payflow.authservice.config.CloudinaryProperties;
import com.payflow.authservice.payload.responseDto.CloudinarySignatureResponse;
import com.payflow.authservice.payload.responseDto.CloudinaryUploadResponse;
import com.payflow.authservice.service.CloudinaryService;
import com.payflow.common.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private final CloudinaryProperties cloudinaryProperties;

    @Override
    public CloudinaryUploadResponse uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Avatar file cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("image/")) {
            throw new BusinessException("Only image files are permitted for avatar upload");
        }

        String publicId = String.format("user_%d_%s", userId, UUID.randomUUID().toString().substring(0, 8));
        String folder = "payflow/avatars";

        try {
            Map<?, ?> uploadParams = ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", folder,
                    "overwrite", true,
                    "resource_type", "image",
                    "transformation", new Transformation<>()
                            .width(400)
                            .height(400)
                            .crop("fill")
                            .gravity("face")
                            .fetchFormat("auto")
                            .quality("auto")
            );

            log.info("Uploading avatar to Cloudinary for userId={}, publicId={}", userId, publicId);
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            String secureUrl = (String) uploadResult.get("secure_url");
            String resultPublicId = (String) uploadResult.get("public_id");
            String format = (String) uploadResult.get("format");
            Integer width = (Integer) uploadResult.get("width");
            Integer height = (Integer) uploadResult.get("height");
            Number bytesNumber = (Number) uploadResult.get("bytes");
            Long bytes = bytesNumber != null ? bytesNumber.longValue() : file.getSize();

            log.info("Successfully uploaded avatar to Cloudinary: secureUrl={}", secureUrl);

            return CloudinaryUploadResponse.builder()
                    .publicId(resultPublicId)
                    .secureUrl(secureUrl)
                    .format(format)
                    .width(width)
                    .height(height)
                    .bytes(bytes)
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload avatar to Cloudinary for userId={}: {}", userId, e.getMessage(), e);
            throw new BusinessException("Failed to upload avatar image to Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public CloudinarySignatureResponse generateSignedUploadParameters(Long userId) {
        long timestamp = System.currentTimeMillis() / 1000L;
        String folder = "payflow/avatars";
        String tags = "avatar,user_" + userId;

        Map<String, Object> paramsToSign = new HashMap<>();
        paramsToSign.put("timestamp", timestamp);
        paramsToSign.put("folder", folder);
        paramsToSign.put("tags", tags);

        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinaryProperties.getApiSecret());
        String uploadUrl = String.format("https://api.cloudinary.com/v1_1/%s/image/upload", cloudinaryProperties.getCloudName());

        log.info("Generated signed Cloudinary upload params for userId={}, folder={}", userId, folder);

        return CloudinarySignatureResponse.builder()
                .cloudName(cloudinaryProperties.getCloudName())
                .apiKey(cloudinaryProperties.getApiKey())
                .timestamp(timestamp)
                .signature(signature)
                .folder(folder)
                .uploadUrl(uploadUrl)
                .parameters(paramsToSign)
                .build();
    }

    @Override
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            log.info("Deleting image from Cloudinary: publicId={}", publicId);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("Failed to delete Cloudinary image publicId={}: {}", publicId, e.getMessage());
        }
    }

    @Override
    public String getTransformedAvatarUrl(String publicId, int width, int height) {
        if (publicId == null || publicId.isBlank()) {
            return null;
        }
        return cloudinary.url()
                .transformation(new Transformation<>()
                        .width(width)
                        .height(height)
                        .crop("fill")
                        .gravity("face")
                        .fetchFormat("auto")
                        .quality("auto"))
                .generate(publicId);
    }
}

