package com.payflow.merchantservice.payload.requestDto;

import com.payflow.merchantservice.model.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycConfirmRequest {

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    private String documentNumber;

    @NotBlank(message = "S3 object key is required")
    private String s3ObjectKey;
}
