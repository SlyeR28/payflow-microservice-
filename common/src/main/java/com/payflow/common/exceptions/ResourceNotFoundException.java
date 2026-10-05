package com.payflow.common.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(message , HttpStatus.NOT_FOUND , "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " +id,
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND");
    }
}
