package com.payflow.authservice.controller;

import com.payflow.authservice.payload.requestDto.*;
import com.payflow.authservice.payload.responseDto.MessageResponse;
import com.payflow.authservice.payload.responseDto.TokenResponse;
import com.payflow.authservice.payload.responseDto.UsernameUpdateResponse;
import com.payflow.authservice.service.AuthService;
import com.payflow.authservice.service.UserNameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;
    private final UserNameService userNameService;


    // public
    @PostMapping("/register")
   public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest registerRequest){
        MessageResponse messageResponse = authService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageResponse);
    }

    // verify email
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest registerRequest) {
        MessageResponse messageResponse = authService.verifyEmail(registerRequest);
        return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
    }

    // resend Otp
    @PostMapping("/resend-otp")
    public ResponseEntity<MessageResponse> resendOtp(@Valid @RequestBody ResendOtpRequest registerRequest) {
        MessageResponse messageResponse = authService.resendOtp(registerRequest);
        return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
    }

    // login
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        TokenResponse login = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(login);
    }

    // refresh token
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        TokenResponse tokenResponse = authService.refreshToken(refreshTokenRequest);
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
    }

    // forgot password
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgetPasswordRequest forgotPasswordRequest) {
        MessageResponse messageResponse = authService.forgotPassword(forgotPasswordRequest);
        return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
    }

    // reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        MessageResponse messageResponse = authService.resetPassword(resetPasswordRequest);
        return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
    }

    // logout
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal Long userId) {
        MessageResponse messageResponse = authService.logout(userId);
        return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
    }

    //change password
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@AuthenticationPrincipal Long userId, @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        MessageResponse messageResponse = authService.changePassword(userId, changePasswordRequest);
        return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
    }

    @PutMapping("/update-user")
    public ResponseEntity<UsernameUpdateResponse> updateUser(@AuthenticationPrincipal Long userId, @Valid @RequestBody UsernameUpdateRequest updateUserRequest) {
        UsernameUpdateResponse usernameUpdateResponse = userNameService.updateUsername(userId, updateUserRequest);
        return ResponseEntity.status(HttpStatus.OK).body(usernameUpdateResponse);
    }

}
