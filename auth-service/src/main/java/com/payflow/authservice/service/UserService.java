package com.payflow.authservice.service;

import com.payflow.authservice.payload.requestDto.UpdateProfileRequest;
import com.payflow.authservice.payload.responseDto.UserResponse;

public interface UserService {

    UserResponse getProfile(Long userId);
    UserResponse updateProfile(Long userId , UpdateProfileRequest request);
}
