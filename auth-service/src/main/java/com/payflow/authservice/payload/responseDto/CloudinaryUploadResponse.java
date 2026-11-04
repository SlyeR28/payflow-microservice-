package com.payflow.authservice.payload.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudinaryUploadResponse {
    private String publicId;
    private String secureUrl;
    private String format;
    private Integer width;
    private Integer height;
    private Long bytes;
}

