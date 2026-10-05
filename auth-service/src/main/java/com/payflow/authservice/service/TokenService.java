package com.payflow.authservice.service;

import com.payflow.authservice.model.entity.User;
import com.payflow.authservice.payload.responseDto.TokenResponse;

public interface TokenService {

    TokenResponse issueTokens(User user);
    TokenResponse refreshTokens(String rawRefreshToken);
    void revokeAllForUser(Long userId , String reason);

}
