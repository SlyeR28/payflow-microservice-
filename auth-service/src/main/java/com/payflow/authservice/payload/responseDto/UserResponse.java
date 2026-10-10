package com.payflow.authservice.payload.responseDto;

import com.payflow.authservice.model.enums.UserStatus;
import com.payflow.common.constant.Roles;
import lombok.*;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String avatarUrl;
    private Set<Roles> roles;
    private UserStatus userStatus;
    private Boolean emailVerified;
    private Boolean isUsernameTemporary;   // true while usernameChangedAt == null
    private Instant createdAt;


}
