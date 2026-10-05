package com.payflow.authservice.service;

public interface EmailService {

    void sendOtpEmail(String to , String otp , String purpose);
    void sendWelcomeEmail(String to , String userName);
    void sendTemporaryUsernameEmail(String to , String userName);
    void sendUsernameReminderEmail(String to , String currentUserName);
}
