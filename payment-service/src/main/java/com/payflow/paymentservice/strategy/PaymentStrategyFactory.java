package com.payflow.paymentservice.strategy;

import com.payflow.paymentservice.exception.UnsupportedPaymentProviderException;
import com.payflow.paymentservice.model.enums.PaymentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class PaymentStrategyFactory {

    private final Map<PaymentProvider, PaymentGatewayStrategy> strategies = new EnumMap<>(PaymentProvider.class);

    public PaymentStrategyFactory(List<PaymentGatewayStrategy> gatewayStrategies) {
        for (PaymentGatewayStrategy strategy : gatewayStrategies) {
            strategies.put(strategy.getProvider(), strategy);
            log.info("Registered payment gateway strategy: [{}] -> {}", strategy.getProvider(), strategy.getClass().getSimpleName());
        }
    }

    public PaymentGatewayStrategy getStrategy(PaymentProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Payment provider cannot be null");
        }
        PaymentGatewayStrategy strategy = strategies.get(provider);
        if (strategy == null) {
            throw new UnsupportedPaymentProviderException(provider);
        }
        return strategy;
    }

    public List<PaymentProvider> getSupportedProviders() {
        return new ArrayList<>(strategies.keySet());
    }
}
