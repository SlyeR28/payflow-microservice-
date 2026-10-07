package com.payflow.merchantservice.repository;

import com.payflow.merchantservice.model.entity.MerchantAddress;
import com.payflow.merchantservice.model.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantAddressRepository extends JpaRepository<MerchantAddress, Long> {

    List<MerchantAddress> findByMerchantId(Long merchantId);

    Optional<MerchantAddress> findByMerchantIdAndAddressType(Long merchantId, AddressType addressType);

    Optional<MerchantAddress> findByMerchantIdAndIsPrimaryTrue(Long merchantId);
}
