package com.payflow.common.security;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtClaims {

    private Long userId;
    private String email;
    private List<String> roles;
    private String jti;
}