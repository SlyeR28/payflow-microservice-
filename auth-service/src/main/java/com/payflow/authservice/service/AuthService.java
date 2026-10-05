package com.payflow.authservice.service;

import com.payflow.authservice.payload.requestDto.*;
import com.payflow.authservice.payload.responseDto.MessageResponse;
import com.payflow.authservice.payload.responseDto.TokenResponse;

public interface AuthService {

    MessageResponse registerUser(RegisterRequest registerRequest);
    MessageResponse verifyEmail(VerifyEmailRequest emailRequest);
    MessageResponse resendOtp(ResendOtpRequest otpRequest);
    TokenResponse login(LoginRequest loginRequest);
    TokenResponse refreshToken(RefreshTokenRequest refreshToken);
    MessageResponse logout(Long userId);
    MessageResponse changePassword( Long userId , ChangePasswordRequest changePasswordRequest);
    MessageResponse forgotPassword(ForgetPasswordRequest forgotPasswordRequest);
    MessageResponse resetPassword(ResetPasswordRequest resetPasswordRequest);
}
