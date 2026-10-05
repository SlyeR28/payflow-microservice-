package com.payflow.authservice.service;

import com.payflow.authservice.model.enums.OtpPurpose;

public interface OtpService {

    void generateAndSend(String email , OtpPurpose purpose);
    void verifyOtp(String email , String submittedOtp , OtpPurpose purpose);

    int cleanupExpired();

}
