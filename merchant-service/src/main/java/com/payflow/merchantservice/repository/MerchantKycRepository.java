package com.payflow.merchantservice.repository;

import com.payflow.merchantservice.model.entity.MerchantKyc;
import com.payflow.merchantservice.model.enums.DocumentType;
import com.payflow.merchantservice.model.enums.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantKycRepository extends JpaRepository<MerchantKyc, Long> {

    List<MerchantKyc> findByMerchantId(Long merchantId);

    Optional<MerchantKyc> findByMerchantIdAndDocumentType(Long merchantId, DocumentType documentType);

    List<MerchantKyc> findByMerchantIdAndStatus(Long merchantId, KycStatus status);

    long countByMerchantIdAndStatus(Long merchantId, KycStatus status);
}
