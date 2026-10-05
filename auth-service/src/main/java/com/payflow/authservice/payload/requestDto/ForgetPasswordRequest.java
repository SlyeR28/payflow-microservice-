package com.payflow.authservice.payload.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ForgetPasswordRequest {

    @Email
    @NotBlank(message = "Email is Required")
    private String email;

}
