package com.payflow.merchantservice.events;

import com.payflow.merchantservice.model.enums.MerchantStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantStatusChangedEvent {

    private Long merchantId;
    private Long userId;
    private MerchantStatus previousStatus;
    private MerchantStatus newStatus;
    private String reason;
    private Instant timestamp;
}
