package com.payflow.commonclient.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JournalClientResponse {

    private String entryReference;
    private String sourceSystem;
    private String entryType;
    private String idempotencyKey;
    private Instant postedAt;


}
