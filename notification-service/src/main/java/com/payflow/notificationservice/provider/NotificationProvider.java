package com.payflow.notificationservice.provider;

import com.payflow.notificationservice.model.document.NotificationMessages;
import com.payflow.notificationservice.model.enums.NotificationChannel;

public interface NotificationProvider {

    /**
     * Identifies which channel this adapter handle (EMAIL ,SMS ,WEBHOOK, IN_APP)
     */
    NotificationChannel getChannel();

    /**
     * Executes the dispatch to external gateways or client sockets.
     */
    ProviderResult send(NotificationMessages messages);

}
