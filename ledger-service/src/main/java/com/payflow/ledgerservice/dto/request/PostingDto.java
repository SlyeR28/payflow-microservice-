package com.payflow.ledgerservice.dto.request;

import com.payflow.ledgerservice.model.enums.PostingDirection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostingDto {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Posting direction is required (DEBIT or CREDIT)")
    private PostingDirection direction;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0001", message = "Amount must be strictly positive")
    private BigDecimal amount;

    @Builder.Default
    private String currency = "INR";
}
