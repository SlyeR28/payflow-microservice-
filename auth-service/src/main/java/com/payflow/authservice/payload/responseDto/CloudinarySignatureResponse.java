package com.payflow.authservice.payload.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudinarySignatureResponse {
    private String cloudName;
    private String apiKey;
    private Long timestamp;
    private String signature;
    private String folder;
    private String uploadUrl;
    private Map<String, Object> parameters;
}

