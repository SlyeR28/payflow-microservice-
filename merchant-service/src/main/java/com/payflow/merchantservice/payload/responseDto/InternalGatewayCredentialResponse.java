package com.payflow.merchantservice.payload.responseDto;

import com.payflow.merchantservice.model.enums.GatewayProvider;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InternalGatewayCredentialResponse {

    private Long merchantId;
    private GatewayProvider gatewayType;
    private String apiKey;
    private String apiSecret;
    private String webhookSecret;
    private Boolean isTestMode;
    private Boolean isActive;
}
