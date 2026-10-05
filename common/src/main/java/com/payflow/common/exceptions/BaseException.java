package com.payflow.common.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base class for all business exceptions in LMS.
 * Extends RuntimeException so @Transactional rolls back by default.
 */
public abstract class BaseException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    protected BaseException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
    }

    protected BaseException(String message, HttpStatus status) {
        this(message, status, status.name());
    }

    protected BaseException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    protected BaseException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}