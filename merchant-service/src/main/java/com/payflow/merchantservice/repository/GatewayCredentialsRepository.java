package com.payflow.merchantservice.repository;

import com.payflow.merchantservice.model.entity.GatewayCredentials;
import com.payflow.merchantservice.model.enums.GatewayProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GatewayCredentialsRepository extends JpaRepository<GatewayCredentials, Long> {

    List<GatewayCredentials> findByMerchantId(Long merchantId);

    Optional<GatewayCredentials> findByMerchantIdAndGatewayType(Long merchantId, GatewayProvider gatewayType);

    Optional<GatewayCredentials> findByMerchantIdAndGatewayTypeAndIsActiveTrue(Long merchantId, GatewayProvider gatewayType);

    boolean existsByMerchantIdAndGatewayType(Long merchantId, GatewayProvider gatewayType);
}
