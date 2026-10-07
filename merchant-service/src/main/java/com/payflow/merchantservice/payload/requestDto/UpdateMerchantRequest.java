package com.payflow.merchantservice.payload.requestDto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateMerchantRequest {

    @Size(max = 200, message = "Business name cannot exceed 200 characters")
    private String businessName;

    @Size(max = 200, message = "Legal name cannot exceed 200 characters")
    private String legalName;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String businessPhone;

    @Size(max = 255, message = "Website cannot exceed 255 characters")
    private String website;

    @Size(max = 100, message = "Business category cannot exceed 100 characters")
    private String businessCategory;
}
