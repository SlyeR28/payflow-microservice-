package com.payflow.commonclient.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LedgerTransactionClientRequest {


    private String sourceSystem;
    private String entryType;
    private String description;
    private String idempotencyKey;
    private List<LedgerPostingClientDto> postings;

}
