package com.payflow.ledgerservice.dto.request;

import com.payflow.ledgerservice.model.enums.EntryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecordTransactionRequest {

    @NotBlank(message = "Source system is required")
    private String sourceSystem;

    @NotNull(message = "Entry type is required")
    private EntryType entryType;

    private String description;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @NotEmpty(message = "Postings cannot be empty")
    @Size(min = 2, message = "Double-entry transaction requires at least 2 postings")
    private List<@Valid PostingDto> postings;
}
