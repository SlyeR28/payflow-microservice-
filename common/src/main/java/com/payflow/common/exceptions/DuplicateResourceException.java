package com.payflow.common.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BaseException {
    public DuplicateResourceException(String message) {
        super(message , HttpStatus.CONFLICT , "DUPLICATE_RESOURCE");
    }

    public DuplicateResourceException(String resource  ,String field , Object value) {
        super(resource + " already exist with " + field + " : " + value ,
                HttpStatus.CONFLICT ,
                "DUPLICATE_RESOURCE");
    }
}
