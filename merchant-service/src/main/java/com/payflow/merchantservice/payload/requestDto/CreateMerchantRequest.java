package com.payflow.merchantservice.payload.requestDto;

import com.payflow.merchantservice.model.enums.BusinessType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateMerchantRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 200, message = "Business name cannot exceed 200 characters")
    private String businessName;

    @NotBlank(message = "Legal name is required")
    @Size(max = 255, message = "Legal name cannot exceed 255 characters")
    private String legalName;

    @NotBlank(message = "Business email is required")
    @Email(message = "Invalid email format")
    private String businessEmail;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String businessPhone;

    @Size(max = 255, message = "Website cannot exceed 255 characters")
    private String website;

    @NotBlank(message = "Business category is required")
    @Size(max = 100, message = "Business category cannot exceed 100 characters")
    private String businessCategory;

    @NotNull(message = "Business type is required")
    private BusinessType businessType;

    @NotBlank(message = "PAN number is required")
    @Size(min = 10, max = 10, message = "PAN must be exactly 10 characters")
    private String panNumber;

    @Size(max = 30, message = "GSTIN cannot exceed 30 characters")
    private String gstin;

    @NotNull(message = "Registered Address is required")
    @Valid
    private AddressRequest addressRequest;
}
