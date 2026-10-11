package com.payflow.ledgerservice.dto.request;

import com.payflow.ledgerservice.model.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 128, message = "Account name must not exceed 128 characters")
    private String name;

    private Long merchantId;

    @NotNull(message = "Account type is required (ASSET, LIABILITY, REVENUE, EXPENSE, EQUITY)")
    private AccountType accountType;

    @Builder.Default
    private String currency = "INR";
}
