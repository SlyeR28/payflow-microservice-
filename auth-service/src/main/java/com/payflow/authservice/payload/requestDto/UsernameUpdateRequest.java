package com.payflow.authservice.payload.requestDto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsernameUpdateRequest {

    @NotBlank(message = "UserName is Required")
    @Size(min = 3 , max = 50 , message = "UserName must be between 3 and 50 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "Only letters, numbers, dots, underscores")
    private String userName;
}

