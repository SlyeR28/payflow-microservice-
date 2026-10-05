package com.payflow.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
Single Source Of truth for endpoints that do Not require JWT.
Injected into SecurityConfig and used by the filter chain
 */
@Configuration
public class publicPathConfig {

    @Bean
    public List<String> publicPaths() {
        return List.of(
                "/api/v1/auth/register",
                "/api/v1/auth/verify-email",
                "/api/v1/auth/resend-otp",
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/api/v1/auth/forgot-password",
                "/api/v1/auth/reset-password",
                "/actuator/health",
                "/actuator/info",
                "/actuator/gateway/**"
        );
    }

}
