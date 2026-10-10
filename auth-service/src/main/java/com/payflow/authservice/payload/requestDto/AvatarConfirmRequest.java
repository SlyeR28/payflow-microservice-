package com.payflow.authservice.payload.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvatarConfirmRequest {

    @NotBlank(message = "S3 object key is required")
    private String s3Key;
}
