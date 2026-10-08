package com.payflow.merchantservice.repository;

import com.payflow.merchantservice.model.entity.Merchant;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    Optional<Merchant> findByBusinessEmail(String businessEmail);

    boolean existsByBusinessEmail(String businessEmail);

    List<Merchant> findByStatus(MerchantStatus status);

    Page<Merchant> findByStatus(MerchantStatus status, Pageable pageable);

    Optional<Merchant> findByPanNumberHash(String panNumberHash);

    boolean existsByPanNumberHash(String panNumberHash);
}

