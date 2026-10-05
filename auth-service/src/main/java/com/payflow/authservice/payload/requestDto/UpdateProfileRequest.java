package com.payflow.authservice.payload.requestDto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(max = 50) private String firstName;
    @Size(max = 50) private String lastName;
    @Size(max = 20) private String phoneNumber;
    @Size(max = 500) private String avatarUrl;

}
