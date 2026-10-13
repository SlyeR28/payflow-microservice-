package com.payflow.notificationservice.mapper;

import com.payflow.notificationservice.dto.response.DeliveryAttemptResponse;
import com.payflow.notificationservice.dto.response.NotificationResponse;
import com.payflow.notificationservice.dto.response.PreferenceResponse;
import com.payflow.notificationservice.dto.response.TemplateResponse;
import com.payflow.notificationservice.model.document.DeliveryAttempt;
import com.payflow.notificationservice.model.document.NotificationMessages;
import com.payflow.notificationservice.model.document.NotificationPreference;
import com.payflow.notificationservice.model.document.NotificationTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "channel", source = "notificationChannel")
    NotificationResponse toNotificationResponse(NotificationMessages message);

    DeliveryAttemptResponse toDeliveryAttemptResponse(DeliveryAttempt attempt);

    List<DeliveryAttemptResponse> toDeliveryAttemptResponseList(List<DeliveryAttempt> attempts);

    TemplateResponse toTemplateResponse(NotificationTemplate template);

    List<TemplateResponse> toTemplateResponseList(List<NotificationTemplate> templates);

    PreferenceResponse toPreferenceResponse(NotificationPreference preference);
}
