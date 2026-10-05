package com.payflow.authservice.service.impl;

import com.payflow.authservice.exceptions.UserNameAlreadyTakenException;
import com.payflow.authservice.exceptions.UserNameCoolDownException;
import com.payflow.authservice.model.entity.User;
import com.payflow.authservice.payload.requestDto.UsernameUpdateRequest;
import com.payflow.authservice.payload.responseDto.UsernameUpdateResponse;
import com.payflow.authservice.repository.UserRepository;
import com.payflow.authservice.service.UserNameService;
import com.payflow.common.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNameServiceImpl implements UserNameService {

    private static final long  COOLDOWN_DAYS = 45;

    private final UserRepository userRepository;

    @Override
    public UsernameUpdateResponse updateUsername(Long userId, UsernameUpdateRequest usernameUpdateRequest) {

        User user = userRepository.findById(userId).
                orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String newUserName = usernameUpdateRequest.getUserName();

        // checking that if username same as existing one
        if (newUserName.equals(user.getUserName())){
            throw new UserNameAlreadyTakenException("UserName Already Exits: " +newUserName);
        }

        // checking is that taken by other or not
        userRepository.findByUserName(newUserName).ifPresent( existingUser -> {
            throw new UserNameAlreadyTakenException("UserName Already Exits: " +newUserName);
        });

        boolean isFirstChange = user.getUserNameChangedAt() == null;

        Instant nextAllowed = null;
        if (!isFirstChange) {
           Instant allowedAt = user.getUserNameChangedAt().plus(COOLDOWN_DAYS , ChronoUnit.DAYS);
           if (allowedAt.isAfter(Instant.now())){
               throw new UserNameCoolDownException(allowedAt);
           }
        }


        // update username
        user.setUserName(newUserName);
        user.setUserNameChangedAt(Instant.now());
        userRepository.save(user);

        if (!isFirstChange){
            nextAllowed = user.getUserNameChangedAt().plus(COOLDOWN_DAYS , ChronoUnit.DAYS);
        }

        log.info("Username updated successfully for userId = {} to {}", userId , newUserName);


        return UsernameUpdateResponse.builder()
                .userName(newUserName)
                .nextChangeAllowedAt(nextAllowed)
                .message(isFirstChange
                ? "Username updated successfully."
                : "Username updated. Next change allowed at " + nextAllowed)
                .build();
    }
}
