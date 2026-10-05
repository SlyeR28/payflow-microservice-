package com.payflow.authservice.payload.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {


    @Size(min = 3, max = 50, message = "UserName must be between 3 and 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "UserName must contain only letters, numbers, and underscores"
    )
    private String userName; // Optional can bee null b/c its auto-generated

    @NotBlank(message = "Email is Required")
    @Email(message = "Email must be valid")
    @Size(max =255, message = "Email must be between 10 and 255 characters")
    private String email;

    @NotBlank(message = "Password is Required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "Confirm Password is Required")
    private String confirmPassword;

    @NotBlank(message = "First Name is Required")
    @Size(max = 50, message = "First Name must be between 1 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last Name is Required")
    @Size(max = 50, message = "Last Name must be between 1 and 50 characters")
    private String lastName;

}
