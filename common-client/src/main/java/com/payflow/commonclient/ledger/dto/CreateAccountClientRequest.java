package com.payflow.commonclient.ledger.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountClientRequest {

    @NotNull(message = "Account Name is Required")
    private String name;

    private Long merchantId;

    @NotNull(message = "Account Type is Required e.g -> ASSET , LIABILITY , REVENUE , EXPENSE , EQUITY")
    private String accountType;

    @Builder.Default
    private String currency = "INR";

}
