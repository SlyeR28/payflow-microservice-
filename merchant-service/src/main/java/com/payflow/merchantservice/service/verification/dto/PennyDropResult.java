package com.payflow.merchantservice.service.verification.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PennyDropResult  extends VerificationResult{

    private Long bankAccountId;
    private String accountNumberMasked;
    private String ifscCode;
    private String bankName;
    private String beneficiaryName;       // Beneficiary name returned by the recipient bank via IMPS/NPCI
    private String utr;                   // Unique Transaction Reference (IMPS ₹1 reference)
    private BigDecimal transferredAmount; // Usually 1.00
    private String bankResponseCode;      // e.g., "00" (Success), "M1" (Account Closed), etc.
}
