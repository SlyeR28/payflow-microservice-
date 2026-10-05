package com.payflow.authservice.security.oauth2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class  OAuth2UserInfoDto {

    private String providerUserId;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private boolean emailVerified;
}
