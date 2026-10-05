package com.payflow.common.audit;

public interface AuditEventPublisher {

    void publish(AuditEvent event);
}