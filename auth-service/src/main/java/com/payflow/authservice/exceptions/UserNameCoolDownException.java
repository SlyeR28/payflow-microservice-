package com.payflow.authservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public class UserNameCoolDownException extends BaseException {

    private final Instant nextChangeAllowedAt;

    public UserNameCoolDownException(Instant nextChangeAllowedAt) {
        super("Username can only be changed once every 45 days. Next change allowed at: "
                + nextChangeAllowedAt, HttpStatus.TOO_MANY_REQUESTS, "USERNAME_CHANGE_COOLDOWN");
        this.nextChangeAllowedAt = nextChangeAllowedAt;
    }

    public Instant getNextChangeAllowedAt() {
        return nextChangeAllowedAt;
    }
}
