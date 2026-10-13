package com.payflow.notificationservice.repository;

import com.payflow.notificationservice.model.document.DeliveryAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryAttemptRepository extends MongoRepository<DeliveryAttempt, String> {

    List<DeliveryAttempt> findByNotificationId(String notificationId);

    List<DeliveryAttempt> findByNotificationReference(String notificationReference);
}
