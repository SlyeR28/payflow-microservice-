package com.payflow.notificationservice.service;

import com.payflow.notificationservice.dto.request.CreateTemplateRequest;
import com.payflow.notificationservice.dto.request.SendNotificationRequest;
import com.payflow.notificationservice.dto.request.UpdatePreferenceRequest;
import com.payflow.notificationservice.dto.response.NotificationResponse;
import com.payflow.notificationservice.dto.response.PreferenceResponse;
import com.payflow.notificationservice.dto.response.TemplateResponse;

public interface NotificationService {

    NotificationResponse sendNotification(SendNotificationRequest request);

    NotificationResponse getNotificationByReference(String referenceId);

    TemplateResponse createTemplate(CreateTemplateRequest request);

    PreferenceResponse updatePreference(Long merchantId, UpdatePreferenceRequest request);

    PreferenceResponse getPreference(Long merchantId);

}
