package com.payflow.ledgerservice.dto.response;

import com.payflow.ledgerservice.model.enums.PostingDirection;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostingResponse {

    private String accountNumber;
    private String accountName;
    private PostingDirection direction;
    private BigDecimal amount;
    private String currency;
    private Instant createdAt;
}
