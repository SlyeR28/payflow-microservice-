package com.payflow.notificationservice.dto.request;

import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.model.enums.TemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTemplateRequest {

    @NotBlank(message = "Template code is required (e.g., PAYMENT_SUCCESS_EMAIL)")
    private String templateCode;

    @NotNull(message = "Template type is required")
    private TemplateType templateType;

    @NotNull(message = "Notification channel is required")
    private NotificationChannel channel;

    private String subject; // Optional for SMS, required for Email

    @NotBlank(message = "Body template is required")
    private String bodyTemplate;

    @Builder.Default
    private boolean active = true;
}
