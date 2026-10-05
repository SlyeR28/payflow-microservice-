package com.payflow.authservice.service.impl;

import com.payflow.authservice.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// this is mock only for checking on that in some time it will get @Async with kakfa event handler

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {



        @Override
        public void sendOtpEmail(String to, String otp, String purpose) {
            log.info("""
                
                ╔═══════════════════════════════════════════╗
                ║         EMAIL (MOCK) — OTP SENT           ║
                ╠═══════════════════════════════════════════╣
                ║ To:      {}
                ║ Purpose: {}
                ║ OTP:     {}
                ╚═══════════════════════════════════════════╝
                """, to, purpose, otp);
        }

        @Override
        public void sendWelcomeEmail(String to, String username) {
            log.info("[MOCK EMAIL] Welcome sent to {} (username: {})", to, username);
        }

        @Override
        public void sendTemporaryUsernameEmail(String to, String username) {
            log.info("""
                
                ╔═══════════════════════════════════════════╗
                ║     EMAIL (MOCK) — TEMP USERNAME          ║
                ╠═══════════════════════════════════════════╣
                ║ To:       {}
                ║ Username: {}
                ║ Message:  Temporary username assigned.
                ║           Please update it as needed.
                ╚═══════════════════════════════════════════╝
                """, to, username);
        }

        @Override
        public void sendUsernameReminderEmail(String to, String currentUsername) {
            log.info("[MOCK EMAIL] Username reminder sent to {} (still using: {})",
                    to, currentUsername);
        }
    }

