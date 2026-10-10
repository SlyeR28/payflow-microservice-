package com.payflow.authservice.service.impl;

import com.payflow.authservice.events.OtpRequestedEvent;
import com.payflow.authservice.exceptions.InvalidOtpException;
import com.payflow.authservice.exceptions.OtpExpiredException;
import com.payflow.authservice.model.entity.OtpCode;
import com.payflow.authservice.model.enums.OtpPurpose;
import com.payflow.authservice.repository.OtpCodeRepository;
import com.payflow.authservice.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OtpServiceImpl  implements OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 5;


    private final OtpCodeRepository otpCodeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void generateAndSend(String email, OtpPurpose purpose) {
       otpCodeRepository.invalidateAll(email , purpose);

       String otp = generateOtp();
       String codeHash = Sha256(otp);

       otpCodeRepository.save(OtpCode.builder()
               .email(email)
               .codeHash(codeHash)
               .purpose(purpose)
               .expiresAt(Instant.now().plus(otpExpiryMinutes , ChronoUnit.MINUTES))
               .consumed(false)
               .attempts(0)
               .build());

       eventPublisher.publishEvent(new OtpRequestedEvent(email , otp , purpose));
       log.info("OTP generated and sent to {} for {}", email, purpose);
    }

    @Override
    public void verifyOtp(String email, String submittedOtp, OtpPurpose purpose) {
        OtpCode otpCode = otpCodeRepository.findLatestValid(email, purpose, Instant.now())
                .orElseThrow(OtpExpiredException::new);

        if (otpCode.getAttempts() >= MAX_ATTEMPTS){
            otpCode.setConsumed(true);
            otpCodeRepository.save(otpCode);
            throw new InvalidOtpException();
        }

        if(!Sha256(submittedOtp).equals(otpCode.getCodeHash())){
            otpCode.setAttempts(otpCode.getAttempts() + 1);
            if (otpCode.getAttempts() >= MAX_ATTEMPTS)
                otpCode.setConsumed(true);
            otpCodeRepository.save(otpCode);
            throw new InvalidOtpException();
        }
        otpCode.setConsumed(true);
        otpCodeRepository.save(otpCode);
    }

    @Transactional
    @Override
    @Scheduled(fixedDelayString = "${app.otp.cleanup-interval-ms:3600000}")
    public int cleanupExpired() {
        return otpCodeRepository.deleteExpiredBefore(Instant.now());
    }


    private String generateOtp() {
       StringBuilder sb = new StringBuilder(OTP_LENGTH);

       for (int i = 0; i<OTP_LENGTH; i++){
           sb.append(secureRandom.nextInt(10));
       }
       return sb.toString();
    }

    private String Sha256(String str) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length*2);
            for (byte b : hash)hex.append(String.format("%02x" , b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable " , e);
        }
    }
}
