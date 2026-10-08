package com.payflow.merchantservice.repository;

import com.payflow.merchantservice.model.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByMerchantId(Long merchantId);

    long countByMerchantId(Long merchantId);

    Optional<BankAccount> findByMerchantIdAndIsPrimaryTrue(Long merchantId);

    Optional<BankAccount> findByIdAndMerchantId(Long id, Long merchantId);

    Optional<BankAccount> findByMerchantIdAndAccountNumberHash(Long merchantId, String accountNumberHash);

    long countByAccountNumberHash(String accountNumberHash);
}
