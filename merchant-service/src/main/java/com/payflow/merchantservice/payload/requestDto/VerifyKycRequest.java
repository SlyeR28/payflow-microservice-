package com.payflow.merchantservice.payload.requestDto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyKycRequest {

    @NotNull(message = "Document ID is required")
    private Long documentId;

    @NotNull(message = "Approval decision is required")
    private Boolean isApproved;

    private String rejectionReason;
}
