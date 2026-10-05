package com.payflow.authservice.service.impl;

import com.payflow.authservice.mapper.UserMapper;
import com.payflow.authservice.model.entity.User;
import com.payflow.authservice.payload.requestDto.UpdateProfileRequest;
import com.payflow.authservice.payload.responseDto.UserResponse;
import com.payflow.authservice.repository.UserRepository;
import com.payflow.authservice.service.UserService;
import com.payflow.common.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getProfile(Long userId) {
       User user = userRepository.findById(userId)
               .orElseThrow(() -> new ResourceNotFoundException("User not found" , userId));
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
}
