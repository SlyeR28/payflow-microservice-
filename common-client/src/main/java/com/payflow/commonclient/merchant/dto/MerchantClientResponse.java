package com.payflow.commonclient.merchant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantClientResponse {

    private Long id;
    private Long userId;
    private String businessName;
    private String legalName;
    private String businessEmail;
    private String status;
    private Boolean isPanVerified;
    private Boolean isBankVerified;
    private Instant approvedAt;
    private String escrowAccountNumber;


}
