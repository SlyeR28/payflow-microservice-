package com.payflow.authservice.security.oauth2;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoogleOAuth2UserInfoExtractor implements OAuth2UserInfoExtractor {

    @Override
    public OAuth2UserInfoDto extract(Map<String, Object> attributes) {
        String sub = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String givenName = (String) attributes.get("given_name");
        String familyName = (String) attributes.get("family_name");
        String picture = (String) attributes.get("picture");

        Object emailVerifiedObj = attributes.get("email_verified");
        boolean emailVerified = false;
        if (emailVerifiedObj instanceof Boolean b) {
            emailVerified = b;
        } else if (emailVerifiedObj instanceof String s) {
            emailVerified = Boolean.parseBoolean(s);
        }

        // Fallback for names if given_name/family_name are not provided separately
        if ((givenName == null || givenName.isBlank()) && attributes.containsKey("name")) {
            String fullName = (String) attributes.get("name");
            if (fullName != null && !fullName.isBlank()) {
                String[] parts = fullName.trim().split("\\s+", 2);
                givenName = parts[0];
                if (parts.length > 1) {
                    familyName = parts[1];
                }
            }
        }

        return OAuth2UserInfoDto.builder()
                .providerUserId(sub)
                .email(email)
                .firstName(givenName != null ? givenName : "")
                .lastName(familyName != null ? familyName : "")
                .avatarUrl(picture)
                .emailVerified(emailVerified)
                .build();
    }
}
