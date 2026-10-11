package com.payflow.ledgerservice.repository;

import com.payflow.ledgerservice.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByMerchantId(Long merchantId);

    boolean existsByAccountNumber(String accountNumber);
}
