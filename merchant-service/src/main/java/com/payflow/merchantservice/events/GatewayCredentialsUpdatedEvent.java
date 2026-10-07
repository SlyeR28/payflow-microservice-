package com.payflow.merchantservice.events;

import com.payflow.merchantservice.model.enums.GatewayProvider;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GatewayCredentialsUpdatedEvent {

    private Long merchantId;
    private GatewayProvider gatewayType;
    private Boolean isActive;
    private Instant timestamp;
}
