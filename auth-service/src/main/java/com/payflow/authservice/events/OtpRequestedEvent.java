package com.payflow.authservice.events;

import com.payflow.authservice.model.enums.OtpPurpose;

public record OtpRequestedEvent(String email , String otp , OtpPurpose otpPurpose) {
}
