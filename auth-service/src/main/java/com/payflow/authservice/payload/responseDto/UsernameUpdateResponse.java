package com.payflow.authservice.payload.responseDto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsernameUpdateResponse {

    private String userName;
    private Instant nextChangeAllowedAt; // null if this was the free first change
    private String message;
}
