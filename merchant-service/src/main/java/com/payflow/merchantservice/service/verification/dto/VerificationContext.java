package com.payflow.merchantservice.service.verification.dto;


import com.payflow.merchantservice.model.enums.BusinessType;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationContext {

    private Long merchantId;
    private Long userId;

    //Name used for Fuzzy Match Comparison
    private String legalName;
    private String businessName;
    private BusinessType businessType;

    // Pan Verification parameters
    private String panNumber;

    // BankAccount / Penny Drop Parameter
    private Long bankId;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;

    // Gstin parameter
    private String gstin;

    // configuring similarity threshold for vendor specific
    @Builder.Default
    private Double nameMatchThreshold = 0.70;

    // Extensible metadata
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

}
