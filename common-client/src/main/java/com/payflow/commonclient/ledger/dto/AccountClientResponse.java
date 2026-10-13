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
public class AccountClientResponse {

    private String accountNumber;
    private String name;
    private Long merchantId;
    private String accountType;
    private String currency;
    private String status;
    private  BigDecimal currentBalance;
    private  BigDecimal availableBalance;

}
