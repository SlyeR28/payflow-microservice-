package com.payflow.authservice.service.impl;

import com.payflow.authservice.mapper.UserMapper;
import com.payflow.authservice.model.entity.User;
import com.payflow.authservice.payload.requestDto.AvatarConfirmRequest;
import com.payflow.authservice.payload.requestDto.AvatarUploadUrlRequest;
import com.payflow.authservice.payload.requestDto.UpdateProfileRequest;
import com.payflow.authservice.payload.responseDto.*;
import com.payflow.authservice.repository.UserRepository;
import com.payflow.authservice.service.CloudinaryService;
import com.payflow.authservice.service.UserService;
import com.payflow.common.constant.Roles;
import com.payflow.common.exceptions.BusinessException;
import com.payflow.common.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", userId));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse addRole(Long userId, Roles roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.addRole(roles);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public AvatarUploadUrlResponse generateAvatarUploadUrl(Long userId, AvatarUploadUrlRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        CloudinarySignatureResponse sig = cloudinaryService.generateSignedUploadParameters(userId);

        log.info("Generated Cloudinary upload URL for userId={}", userId);

        return AvatarUploadUrlResponse.builder()
                .uploadUrl(sig.getUploadUrl())
                .s3Key("payflow/avatars/user_" + userId)
                .expiresInMinutes(15)
                .build();
    }

    @Override
    public UserResponse confirmAvatarUpload(Long userId, AvatarConfirmRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setAvatarUrl(request.getS3Key());
        User savedUser = userRepository.save(user);

        log.info("Confirmed avatar upload for userId={}, url={}", userId, request.getS3Key());
        return userMapper.toResponse(savedUser);
    }

    @Override
    public AvatarDownloadUrlResponse getAvatarDownloadUrl(Long userId) {
        return getAvatarDownloadUrlByUserId(userId);
    }

    @Override
    public AvatarDownloadUrlResponse getAvatarDownloadUrlByUserId(Long targetUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        if (user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) {
            throw new BusinessException("User does not have an avatar uploaded");
        }

        String avatarUrl = user.getAvatarUrl();

        return AvatarDownloadUrlResponse.builder()
                .downloadUrl(avatarUrl)
                .s3Key(avatarUrl)
                .expiresInMinutes(0)
                .build();
    }

    @Override
    public UserResponse uploadAvatarCloudinary(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        CloudinaryUploadResponse uploadResponse = cloudinaryService.uploadAvatar(userId, file);
        user.setAvatarUrl(uploadResponse.getSecureUrl());
        User savedUser = userRepository.save(user);

        log.info("Updated user avatar via Cloudinary: userId={}, secureUrl={}", userId, uploadResponse.getSecureUrl());
        return userMapper.toResponse(savedUser);
    }

    @Override
    public CloudinarySignatureResponse getCloudinaryUploadSignature(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return cloudinaryService.generateSignedUploadParameters(userId);
    }
}
