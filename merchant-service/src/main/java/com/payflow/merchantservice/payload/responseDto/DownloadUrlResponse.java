package com.payflow.merchantservice.payload.responseDto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DownloadUrlResponse {

    private String downloadUrl;
    private String s3Key;
    private int expiryInMinutes;

}
