package com.payflow.authservice.service.impl;

import com.payflow.authservice.exceptions.InvalidCredentialsException;
import com.payflow.authservice.model.entity.RefreshToken;
import com.payflow.authservice.model.entity.User;
import com.payflow.authservice.payload.responseDto.TokenResponse;
import com.payflow.authservice.repository.RefreshTokenRepository;
import com.payflow.authservice.service.TokenService;
import com.payflow.common.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl  implements TokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.expiration-millis}")
    private long accessTokenExpiryMillis;

    @Value("${app.refresh-token.expiration-millis}")
    private long refreshTokenExpiryMillis;

    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public TokenResponse issueTokens(User user) {
        String accessToken = jwtProvider.generateToken(
                user.getId(), user.getEmail(), List.of(user.getRole().name()));

        String familyId = UUID.randomUUID().toString();
        String rawRefreshToken = generateRefreshToken();

        refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(sha256(rawRefreshToken))
                .user(user)
                .familyId(familyId)
                .expiresAt(Instant.now().plus(refreshTokenExpiryMillis, ChronoUnit.MILLIS))
                .revoked(false)
                .build());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiryMillis / 1000)
                .build();
    }

    @Override
    @Transactional
    public TokenResponse refreshTokens(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidCredentialsException::new);

        if (Boolean.TRUE.equals(token.getRevoked())) {
            refreshTokenRepository.revokeFamily(token.getFamilyId(), "THEFT_DETECTED");
            log.warn("Refresh token reuse detected for user {}. Family {} revoked.",
                    token.getUser().getUserName(), token.getFamilyId());
            throw new InvalidCredentialsException();
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            token.setRevoked(true);
            token.setRevokedReason("EXPIRED");
            refreshTokenRepository.save(token);
            throw new InvalidCredentialsException();
        }

        User user = token.getUser();
        token.setRevoked(true);
        token.setRevokedReason("ROTATED");
        refreshTokenRepository.save(token);

        String newAccessToken = jwtProvider.generateToken(
                user.getId(), user.getEmail(), List.of(user.getRole().name()));

        String newRawRefresh = generateRefreshToken();
        refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(sha256(newRawRefresh))
                .user(user)
                .familyId(token.getFamilyId())
                .expiresAt(Instant.now().plus(refreshTokenExpiryMillis, ChronoUnit.MILLIS))
                .revoked(false)
                .build());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRawRefresh)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiryMillis / 1000)
                .build();
    }

    @Override
    @Transactional
    public void revokeAllForUser(Long userId, String reason) {
        refreshTokenRepository.revokeAllForUser(userId, reason);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
