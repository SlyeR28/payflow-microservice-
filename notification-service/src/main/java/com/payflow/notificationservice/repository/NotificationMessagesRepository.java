package com.payflow.notificationservice.repository;

import com.payflow.notificationservice.model.document.NotificationMessages;
import com.payflow.notificationservice.model.enums.NotificationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationMessagesRepository extends MongoRepository<NotificationMessages, String> {

    Optional<NotificationMessages> findByNotificationReference(String notificationReference);

    Optional<NotificationMessages> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<NotificationMessages> findByMerchantId(Long merchantId);

    List<NotificationMessages> findByStatus(NotificationStatus status);

    List<NotificationMessages> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);
}
