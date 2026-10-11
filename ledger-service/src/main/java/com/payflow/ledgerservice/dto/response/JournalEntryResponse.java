package com.payflow.ledgerservice.dto.response;

import com.payflow.ledgerservice.model.enums.EntryType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JournalEntryResponse {

    private String entryReference;
    private String sourceSystem;
    private EntryType entryType;
    private String description;
    private String idempotencyKey;
    private Instant postedAt;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private boolean balanced;
    private List<PostingResponse> postings;
}
