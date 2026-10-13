package com.payflow.notificationservice.provider;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProviderResult {

    private final boolean success;
    private final String providerName;
    private final String providerMessageId;
    private final Integer statusCode;
    private final String responseBody;
    private final long latencyMs;


    public static ProviderResult success(String providerName ,String messageId , int statusCode ,long latencyMs){
        return ProviderResult.builder()
                .success(true)
                .providerName(providerName)
                .providerMessageId(messageId)
                .statusCode(statusCode)
                .responseBody("DELIVERED")
                .latencyMs(latencyMs)
                .build();

    }

    public static ProviderResult failure(String providerName ,String error , int statusCode ,long latencyMs){
        return ProviderResult.builder()
                .success(false)
                .providerName(providerName)
                .statusCode(statusCode)
                .responseBody(error)
                .latencyMs(latencyMs)
                .build();
    }
}
