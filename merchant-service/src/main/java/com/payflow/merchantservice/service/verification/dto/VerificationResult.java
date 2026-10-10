package com.payflow.merchantservice.service.verification.dto;

import com.payflow.merchantservice.service.verification.VerificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResult {


    private VerificationType verificationType;
    private boolean successful;
    private String referenceId;
    private Double nameMatchScore;
    private boolean nameMatched;
    private String registeredName;
    private String failureReason;

    @Builder.Default
    private Instant executedAt = Instant.now();

    private Map<String , Object>rawResponse;

    public static VerificationResult failure(VerificationType type , String reason){
        return VerificationResult.builder()
                .verificationType(type)
                .successful(false)
                .failureReason(reason)
                .executedAt(Instant.now())
                .build();
    }


}
