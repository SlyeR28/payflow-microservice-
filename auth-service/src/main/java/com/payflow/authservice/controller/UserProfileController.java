package com.payflow.authservice.controller;

import com.payflow.authservice.payload.requestDto.UpdateProfileRequest;
import com.payflow.authservice.payload.responseDto.UserResponse;
import com.payflow.authservice.service.UserService;
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
    @GetMapping
    public ResponseEntity<UserResponse> getUserProfile(@AuthenticationPrincipal Long userId) {
        UserResponse userProfileResponse = userService.getProfile(userId);
        return ResponseEntity.status(HttpStatus.OK).body(userProfileResponse);
    }

    // update profile
    @PutMapping
    public ResponseEntity<UserResponse> updateUserProfile(@AuthenticationPrincipal Long userId,
                                                          @Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        UserResponse userProfileResponse = userService.updateProfile(userId, updateProfileRequest);
        return ResponseEntity.status(HttpStatus.OK).body(userProfileResponse);
    }

}
