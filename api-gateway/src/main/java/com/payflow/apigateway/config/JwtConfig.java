package com.payflow.apigateway.config;

import com.payflow.common.security.JwtProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
*Wires the shared Jwt Provider (from common) with the gateway's secret
*The secret is read from the JWT_SECRET environment variable.
* Fallback default is for local dev only — never use in production. */

@Configuration
public class JwtConfig {


    @Bean
    public JwtProvider jwtProvider(
            @Value("${JWT_SECRET:dev-only-secret-change-me-this-must-be-at-least-64-characters-for-hs256}") String secret,
            @Value("${JWT_EXPIRATION_MILLIS:900000}") long expirationMillis) {

        return new JwtProvider(secret, expirationMillis);
    }

}
