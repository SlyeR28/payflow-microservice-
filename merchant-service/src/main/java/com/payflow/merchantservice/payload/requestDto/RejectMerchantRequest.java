package com.payflow.merchantservice.payload.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RejectMerchantRequest {

    @NotBlank(message = "Rejection reason is required")
    private String reason;
}
