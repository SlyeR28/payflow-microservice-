package com.payflow.notificationservice.exception;

import com.payflow.common.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class TemplateNotFoundException extends BaseException {

    public TemplateNotFoundException(String templateCode) {
        super(String.format("Notification template [%s] was not found or is inactive", templateCode),
                HttpStatus.NOT_FOUND, "TEMPLATE_NOT_FOUND");
    }
}
