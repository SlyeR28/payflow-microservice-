package com.payflow.notificationservice.repository;

import com.payflow.notificationservice.model.document.NotificationPreference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends MongoRepository<NotificationPreference, String> {

    Optional<NotificationPreference> findByMerchantId(Long merchantId);

    boolean existsByMerchantId(Long merchantId);
}
