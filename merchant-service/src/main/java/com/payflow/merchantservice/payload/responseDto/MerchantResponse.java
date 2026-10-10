package com.payflow.merchantservice.payload.responseDto;

import com.payflow.merchantservice.model.enums.BusinessType;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantResponse {

    private Long id;
    private Long userId;
    private String businessName;
    private String legalName;
    private String businessEmail;
    private String businessPhone;
    private String website;
    private String businessCategory;
    private String panNumberMasked;
    private String gstin;
    private MerchantStatus status;
    private BusinessType businessType;
    private Boolean isPanVerified;
    private Instant panVerifiedAt;
    private Boolean isBankVerified;
    private Instant bankVerifiedAt;
    private String rejectReason;
    private Instant approvedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<AddressResponse> addresses;
}
