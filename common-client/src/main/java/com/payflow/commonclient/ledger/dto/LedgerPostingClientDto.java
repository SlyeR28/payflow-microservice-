package com.payflow.commonclient.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LedgerPostingClientDto {

    private String accountNumber;
    private String direction;
    private BigDecimal amount;
    @Builder.Default
    private String currency = "INR";

}
