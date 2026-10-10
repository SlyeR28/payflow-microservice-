package com.payflow.merchantservice.scheduler;

import com.payflow.merchantservice.model.entity.Merchant;
import com.payflow.merchantservice.model.entity.MerchantKyc;
import com.payflow.merchantservice.model.enums.KycStatus;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import com.payflow.merchantservice.repository.MerchantKycRepository;
import com.payflow.merchantservice.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class KycAuditScheduler {

    private final MerchantRepository merchantRepository;
    private final MerchantKycRepository merchantKycRepository;



    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void processPendingKycVerifications() {

        log.info("Starting Scheduled Kyc Audit run ....");
        List<Merchant> underReviewMerchants = merchantRepository.findByStatus(MerchantStatus.UNDER_REVIEW);

        for (Merchant merchant : underReviewMerchants){

            try{
                List<MerchantKyc> docs = merchantKycRepository.findByMerchantId(merchant.getId());

                if (docs.isEmpty()){
                    continue;
                }

                boolean anyRejected = docs.stream().anyMatch(d -> d.getStatus() == KycStatus.REJECTED);

                if (anyRejected){
                    merchant.setStatus(MerchantStatus.REJECTED);
                    merchant.setRejectReason("One or more KYC documents were rejected during compliance audit");
                    merchantRepository.save(merchant);
                    log.warn("Merchant {} auto-rejected during audit due to rejected KYC documents", merchant.getId());
                    continue;
                }

                boolean allDocsVerified = docs.stream().allMatch(d -> d.getStatus() == KycStatus.VERIFIED);
                boolean panVerified = Boolean.TRUE.equals(merchant.getIsPanVerified());
                boolean bankVerified = Boolean.TRUE.equals(merchant.getIsBankVerified());

                if (allDocsVerified && panVerified && bankVerified){
                    merchant.setStatus(MerchantStatus.ACTIVE);
                    merchant.setApprovedAt(Instant.now());
                    merchant.setApprovedBy(1L);
                    merchantRepository.save(merchant);
                    log.info("Merchant {} auto-activated during audit: all KYC documents and verifications passed", merchant.getId());
                }

            } catch (Exception e) {
                log.error("Error During KYC audit for merchantId = {}:{}", merchant.getId(), e.getMessage() , e );
            }
        }

        log.info("Finished scheduled KYC audit run for {} merchants", underReviewMerchants.size());

    }


}
