package com.payflow.merchantservice.service.verification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PanVerificationResult extends VerificationResult {

    private String panNumber;
    private String panStatus;    // "VALID", "INVALID", "EXISTING_AND_VALID
    private String panCategory;  // "INDIVIDUAL", "COMPANY", "GOVERNMENT" (4thCharacter if PAN)
    private String registeredNameOnPan; // exact legal name registered with the government
    private LocalDate dateOfIncorporationOrBirth;
    private boolean aadhaarSeeded;   // Required for individual proprietorships



}
