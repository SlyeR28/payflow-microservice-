package com.payflow.merchantservice.payload.responseDto;

import com.payflow.merchantservice.model.enums.GatewayProvider;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GatewayCredentialsResponse {

    private Long id;
    private GatewayProvider gatewayType;
    private String maskedApiKey;
    private Boolean isActive;
    private Boolean isTestMode;
    private Instant verifiedAt;
    private Instant createdAt;
}
