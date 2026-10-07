package com.payflow.merchantservice.payload.requestDto;

import com.payflow.merchantservice.model.enums.GatewayProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddGatewayCredentialsRequest {

    @NotNull(message = "Gateway type is required")
    private GatewayProvider gatewayType;

    @NotBlank(message = "API key is required")
    private String apiKey;

    @NotBlank(message = "API secret is required")
    private String apiSecret;

    private String webhookSecret;

    private Boolean isTestMode;
}
