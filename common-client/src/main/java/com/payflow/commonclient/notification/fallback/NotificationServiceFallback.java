package com.payflow.commonclient.notification.fallback;

import com.payflow.common.dto.ApiResponse;
import com.payflow.commonclient.notification.NotificationServiceClient;
import com.payflow.commonclient.notification.dto.NotificationClientRequest;
import com.payflow.commonclient.notification.dto.NotificationClientResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationServiceFallback implements NotificationServiceClient {


    @Override
    public ApiResponse<NotificationClientResponse> sendNotification(NotificationClientRequest request) {
       log.warn("NotificationServiceClient fallback triggered for recipient: {}. Notification buffered/dropped." ,
               request.getRecipient());
        return ApiResponse.<NotificationClientResponse>builder()
                .success(false)
                .message("Notification service is currently unreachable. Notification buffered/dropped.")
                .build();
    }
}
