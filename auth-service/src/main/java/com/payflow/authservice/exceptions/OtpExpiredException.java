package com.payflow.authservice.exceptions;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class OtpExpiredException extends BaseException {
    public OtpExpiredException() {
        super("Otp has Expired. Please request a new one " , HttpStatus.BAD_REQUEST , "OTP_EXPIRED");
    }
}
