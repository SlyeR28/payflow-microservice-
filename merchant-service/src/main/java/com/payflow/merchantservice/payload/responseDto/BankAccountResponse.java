package com.payflow.merchantservice.payload.responseDto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankAccountResponse {

    private Long id;
    private Long merchantId;

    private String accountHolderName;
    private String accountNumberLast4;
    private String ifscCode;
    private String bankName;


    private String beneficiaryName; // return by bank after 1 penny drop
    private double nameMatchScore;

    private Boolean isPrimary;
    private Boolean isVerified;

    private Instant createdAt;
    private Instant updatedAt;

}
