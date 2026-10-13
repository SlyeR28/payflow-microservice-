package com.payflow.notificationservice.dto.request;

import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.model.enums.NotificationPriority;
import com.payflow.notificationservice.model.enums.TemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendNotificationRequest {

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    private Long merchantId;

    @NotBlank(message = "Recipient (email, phone, webhook URL, or user ID) is required")
    private String recipient;

    @NotNull(message = "Notification channel is required")
    private NotificationChannel channel;

    @NotNull(message = "Template type is required")
    private TemplateType templateType;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    private String subject; // Optional custom subject override

    private String customContent; // Optional raw content override

    @Builder.Default
    private Map<String, Object> payload = new HashMap<>(); // Dynamic variables e.g. {orderId, amount, customerName}
}
