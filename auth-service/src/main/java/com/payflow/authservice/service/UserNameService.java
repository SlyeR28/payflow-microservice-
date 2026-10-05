package com.payflow.authservice.service;

import com.payflow.authservice.payload.requestDto.UsernameUpdateRequest;
import com.payflow.authservice.payload.responseDto.UsernameUpdateResponse;

public interface UserNameService {

    UsernameUpdateResponse updateUsername(Long userId, UsernameUpdateRequest usernameUpdateRequest);

}
