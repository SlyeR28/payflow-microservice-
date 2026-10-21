package com.payflow.authservice.service;

import com.payflow.authservice.exceptions.InvalidCredentialsException;
import com.payflow.authservice.model.entity.RefreshToken;
import com.payflow.authservice.model.entity.User;
import com.payflow.common.constant.Roles;
import com.payflow.authservice.payload.responseDto.TokenResponse;
import com.payflow.authservice.repository.RefreshTokenRepository;
import com.payflow.authservice.service.impl.TokenServiceImpl;
import com.payflow.common.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "accessTokenExpiryMillis", 900000L);
        ReflectionTestUtils.setField(tokenService, "refreshTokenExpiryMillis", 604800000L);

        sampleUser = User.builder()
                .id(101L)
                .email("alex@payflow.com")
                .userName("alex_trader")
                .roles(Set.of(Roles.MERCHANT))
                .build();
    }

    @Test
    @DisplayName("issueTokens: successfully issues JWT and persists hashed refresh token")
    void testIssueTokensSuccess() {
        when(jwtProvider.generateToken(eq(101L), eq("alex@payflow.com"), anyList()))
                .thenReturn("mocked.jwt.access.token");

        TokenResponse response = tokenService.issueTokens(sampleUser);

        assertNotNull(response);
        assertEquals("mocked.jwt.access.token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900L, response.getExpiresIn());

        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("refreshTokens: successfully rotates valid refresh token")
    void testRefreshTokensSuccess() {
        String rawToken = "raw-refresh-token-xyz-1234567890";
        RefreshToken existingToken = RefreshToken.builder()
                .id(1L)
                .user(sampleUser)
                .familyId("fam-101")
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(existingToken));
        when(jwtProvider.generateToken(eq(101L), eq("alex@payflow.com"), anyList()))
                .thenReturn("new.jwt.access.token");

        TokenResponse response = tokenService.refreshTokens(rawToken);

        assertNotNull(response);
        assertEquals("new.jwt.access.token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertTrue(existingToken.getRevoked());
        assertEquals("ROTATED", existingToken.getRevokedReason());

        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("refreshTokens: detects token reuse / theft and revokes family")
    void testRefreshTokensTheftDetected() {
        String rawToken = "reused-stolen-token";
        RefreshToken revokedToken = RefreshToken.builder()
                .id(2L)
                .user(sampleUser)
                .familyId("fam-theft-999")
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(true)
                .revokedReason("ROTATED")
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revokedToken));

        assertThrows(InvalidCredentialsException.class, () -> tokenService.refreshTokens(rawToken));

        verify(refreshTokenRepository).revokeFamily("fam-theft-999", "THEFT_DETECTED");
        verify(jwtProvider, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("refreshTokens: expired token throws InvalidCredentialsException and is marked revoked")
    void testRefreshTokensExpired() {
        String rawToken = "expired-token-abc";
        RefreshToken expiredToken = RefreshToken.builder()
                .id(3L)
                .user(sampleUser)
                .familyId("fam-expired")
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expiredToken));

        assertThrows(InvalidCredentialsException.class, () -> tokenService.refreshTokens(rawToken));

        assertTrue(expiredToken.getRevoked());
        assertEquals("EXPIRED", expiredToken.getRevokedReason());
        verify(refreshTokenRepository).save(expiredToken);
    }

    @Test
    @DisplayName("revokeAllForUser: delegates to repository")
    void testRevokeAllForUser() {
        tokenService.revokeAllForUser(101L, "USER_LOGOUT");
        verify(refreshTokenRepository).revokeAllForUser(101L, "USER_LOGOUT");
    }
}
