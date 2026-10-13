package com.payflow.commonclient.notification;

import com.payflow.common.dto.ApiResponse;
import com.payflow.commonclient.notification.dto.NotificationClientRequest;
import com.payflow.commonclient.notification.dto.NotificationClientResponse;
import com.payflow.commonclient.notification.fallback.NotificationServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service" ,fallback = NotificationServiceFallback.class)
public interface NotificationServiceClient {


    @PostMapping("/api/v1/notifications/send")
    ApiResponse<NotificationClientResponse> sendNotification(@RequestBody NotificationClientRequest request);

}
