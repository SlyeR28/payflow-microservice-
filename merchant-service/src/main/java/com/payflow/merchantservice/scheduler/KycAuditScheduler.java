package com.payflow.merchantservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class KycAuditScheduler {

    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void processPendingKycVerifications() {
        // TODO: Implement scheduled background task for pending KYC verifications
    }
}
