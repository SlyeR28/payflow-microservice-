package com.payflow.authservice.payload.responseDto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvatarUploadUrlResponse {

    private String uploadUrl;
    private String s3Key;
    private int expiresInMinutes;
}
