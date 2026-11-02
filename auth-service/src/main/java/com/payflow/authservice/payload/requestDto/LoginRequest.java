package com.payflow.authservice.payload.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    /**
     * Can be username OR email.
     * The service decides which one to look up based on whether "@" is present.
     */
    @NotBlank(message = "Username or email is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }
}
