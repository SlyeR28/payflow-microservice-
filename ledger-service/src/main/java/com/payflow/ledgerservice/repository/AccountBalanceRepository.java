package com.payflow.ledgerservice.repository;

import com.payflow.ledgerservice.model.entity.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {

    Optional<AccountBalance> findByAccountId(Long accountId);

    Optional<AccountBalance> findByAccount_AccountNumber(String accountNumber);
}
