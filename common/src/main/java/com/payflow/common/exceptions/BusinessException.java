package com.payflow.common.exceptions;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(message , HttpStatus.BAD_REQUEST , "BUSINESS_ERROR");
    }

    public BusinessException(String message  ,String errorCode) {
        super(message , HttpStatus.BAD_REQUEST , errorCode);
    }
}
