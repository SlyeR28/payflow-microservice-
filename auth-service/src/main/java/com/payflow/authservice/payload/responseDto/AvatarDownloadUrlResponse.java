package com.payflow.authservice.payload.responseDto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvatarDownloadUrlResponse {

    private String downloadUrl;
    private String s3Key;
    private int expiresInMinutes;
}
