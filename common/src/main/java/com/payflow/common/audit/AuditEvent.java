package com.payflow.common.audit;

import lombok.*;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditEvent {

    private String eventId;
    private AuditEventType eventType;
    private Long userId;
    private String service;
    private Map<String, Object> details;
    private Instant timeStamp;

}
