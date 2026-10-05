package com.payflow.authservice.payload.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordRequest {

    @NotBlank(message = "Current Password is Required")
    private String currentPassword;
    @NotBlank(message = "New Password is Required")
    @Size(min = 8, max = 100, message = "New Password must be between 8 and 100 characters")
    private String newPassword;
    @NotBlank(message = "Confirm New Password is Required")
    private String confirmNewPassword;
}
