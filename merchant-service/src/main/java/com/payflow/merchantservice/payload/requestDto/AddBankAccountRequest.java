package com.payflow.merchantservice.payload.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddBankAccountRequest {

    @NotBlank(message = "account holder name is required")
    @Size(max = 150, message = "account holder name cannot exceed 150 characters")
    private String accountHolderName;

    @NotBlank(message = "Account number is required")
    @Size(min = 9, max = 20, message = "Account number must be between 9 and 20 digits")
    @Pattern(regexp = "^[0-9]+$", message = "Account number must contain only digits")
    private String accountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format")
    private String ifscCode;

    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;


    private Boolean isPrimary;
}
