package com.payflow.merchantservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateAddressException extends BaseException {
    public DuplicateAddressException(String message) {
        super(message);
    }
}

