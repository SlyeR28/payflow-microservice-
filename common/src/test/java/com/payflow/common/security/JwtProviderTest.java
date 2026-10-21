package com.payflow.common.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private static final String SECRET_256 = "cGF5Zmxvdy1zZWNyZXQta2V5LTIwMjYtc3VwZXItc2VjdXJlLWtleS0yNTY=";
    private static final long ONE_HOUR_MS = 3600_000L;

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET_256, ONE_HOUR_MS);
    }

    @Test
    @DisplayName("generateToken and parseToken: Should issue valid token with correct claims")
    void generateToken_And_parseToken_Success() {
        Long userId = 101L;
        String email = "trader@payflow.com";
        List<String> roles = List.of("MERCHANT", "CUSTOMER");

        String token = jwtProvider.generateToken(userId, email, roles);

        assertThat(token).isNotBlank();
        assertThat(jwtProvider.isValid(token)).isTrue();

        JwtClaims claims = jwtProvider.parseToken(token);
        assertThat(claims).isNotNull();
        assertThat(claims.getUserId()).isEqualTo(userId);
        assertThat(claims.getEmail()).isEqualTo(email);
        assertThat(claims.getRoles()).containsExactlyInAnyOrder("MERCHANT", "CUSTOMER");
        assertThat(claims.getJti()).isNotBlank();
    }

    @Test
    @DisplayName("isValid: Should return false when token signature is tampered")
    void isValid_TamperedToken_ReturnsFalse() {
        String token = jwtProvider.generateToken(1L, "user@payflow.com", List.of("CUSTOMER"));
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertThat(jwtProvider.isValid(tamperedToken)).isFalse();
        assertThatThrownBy(() -> jwtProvider.parseToken(tamperedToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("isValid: Should return false when token is expired")
    void isValid_ExpiredToken_ReturnsFalse() {
        // Create a provider that issues already-expired tokens
        JwtProvider expiredProvider = new JwtProvider(SECRET_256, -10_000L);
        String expiredToken = expiredProvider.generateToken(1L, "user@payflow.com", List.of("CUSTOMER"));

        assertThat(jwtProvider.isValid(expiredToken)).isFalse();
        assertThatThrownBy(() -> jwtProvider.parseToken(expiredToken))
                .isInstanceOf(JwtException.class);
    }
}
