package com.payflow.notificationservice.exception;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class NotificationNotFoundException extends BaseException {

    public NotificationNotFoundException(String reference) {
        super(String.format("Notification with reference [%s] was not found", reference),
                HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND");
    }
}
