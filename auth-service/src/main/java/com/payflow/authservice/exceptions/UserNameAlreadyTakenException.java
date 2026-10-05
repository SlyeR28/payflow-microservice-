package com.payflow.authservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class UserNameAlreadyTakenException extends BaseException {

    public UserNameAlreadyTakenException(String userName) {
        super("UserName Already Taken: " +userName , HttpStatus.CONFLICT , "USER_NAME_ALREADY_TAKEN");
    }
}
