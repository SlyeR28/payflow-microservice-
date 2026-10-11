package com.payflow.ledgerservice.dto.response;

import com.payflow.ledgerservice.model.enums.AccountStatus;
import com.payflow.ledgerservice.model.enums.AccountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountResponse {

    private String accountNumber;
    private String name;
    private Long merchantId;
    private AccountType accountType;
    private String currency;
    private AccountStatus status;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
    private BigDecimal pendingBalance;
    private Instant createdAt;
    private Instant updatedAt;
}
