package com.payflow.merchantservice.events;

import com.payflow.merchantservice.model.enums.DocumentType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycDocumentSubmittedEvent {

    private Long merchantId;
    private Long documentId;
    private DocumentType documentType;
    private Instant timestamp;
}
