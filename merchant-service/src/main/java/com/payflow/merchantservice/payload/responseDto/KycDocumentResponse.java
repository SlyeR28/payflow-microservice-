package com.payflow.merchantservice.payload.responseDto;

import com.payflow.merchantservice.model.enums.DocumentType;
import com.payflow.merchantservice.model.enums.KycStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycDocumentResponse {

    private Long id;
    private DocumentType documentType;
    private String documentNumberMasked;
    private String s3ObjectKey;
    private String contentType;
    private Long fileSize;
    private KycStatus status;
    private String rejectionReason;
    private Instant verifiedAt;
    private Instant createdAt;
}
