package com.payflow.notificationservice.provider;

import com.payflow.notificationservice.model.enums.NotificationChannel;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ProviderRegistry {

    private final Map<NotificationChannel , NotificationProvider> providerMap =
            new EnumMap<>(NotificationChannel.class);

    public ProviderRegistry(List<NotificationProvider> providers) {
        for (NotificationProvider provider : providers) {
            providerMap.put(provider.getChannel(), provider);
        }
    }

    public Optional<NotificationProvider> getProvider(NotificationChannel channel){
        return Optional.ofNullable(providerMap.get(channel));

    }

}
