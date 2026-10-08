package com.payflow.merchantservice.payload.responseDto;

import com.payflow.merchantservice.model.enums.MerchantStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycStatusResponse {

    private Long merchantId;
    private MerchantStatus merchantStatus;
    private Boolean isPanVerified;
    private long totalDocuments;
    private long verifiedDocuments;
    private long pendingDocuments;
    private long rejectedDocuments;
    private boolean allVerified;
    private List<KycDocumentResponse> documents;
}
