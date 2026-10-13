package com.payflow.notificationservice.repository;

import com.payflow.notificationservice.model.document.NotificationTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends MongoRepository<NotificationTemplate, String> {

    Optional<NotificationTemplate> findByTemplateCode(String templateCode);

    Optional<NotificationTemplate> findByTemplateCodeAndActiveTrue(String templateCode);

    boolean existsByTemplateCode(String templateCode);
}
