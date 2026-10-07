package com.payflow.merchantservice.payload.responseDto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadUrlResponse {

    private String uploadUrl;
    private String s3Key;
    private int expiresInMinutes;
}
