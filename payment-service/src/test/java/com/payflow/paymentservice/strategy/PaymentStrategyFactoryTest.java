package com.payflow.paymentservice.strategy;

import com.payflow.paymentservice.exception.UnsupportedPaymentProviderException;
import com.payflow.paymentservice.model.enums.PaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentStrategyFactoryTest {

    @Mock
    private PaymentGatewayStrategy stripeStrategy;

    @Mock
    private PaymentGatewayStrategy razorpayStrategy;

    @Mock
    private PaymentGatewayStrategy mockStrategy;

    private PaymentStrategyFactory factory;

    @BeforeEach
    void setUp() {
        when(stripeStrategy.getProvider()).thenReturn(PaymentProvider.STRIPE);
        when(razorpayStrategy.getProvider()).thenReturn(PaymentProvider.RAZORPAY);
        when(mockStrategy.getProvider()).thenReturn(PaymentProvider.MOCK);

        factory = new PaymentStrategyFactory(List.of(stripeStrategy, razorpayStrategy, mockStrategy));
    }

    @Test
    @DisplayName("Should resolve Stripe strategy successfully")
    void shouldResolveStripeStrategy() {
        PaymentGatewayStrategy strategy = factory.getStrategy(PaymentProvider.STRIPE);
        assertThat(strategy).isSameAs(stripeStrategy);
    }

    @Test
    @DisplayName("Should resolve Razorpay strategy successfully")
    void shouldResolveRazorpayStrategy() {
        PaymentGatewayStrategy strategy = factory.getStrategy(PaymentProvider.RAZORPAY);
        assertThat(strategy).isSameAs(razorpayStrategy);
    }

    @Test
    @DisplayName("Should resolve Mock strategy successfully")
    void shouldResolveMockStrategy() {
        PaymentGatewayStrategy strategy = factory.getStrategy(PaymentProvider.MOCK);
        assertThat(strategy).isSameAs(mockStrategy);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when provider is null")
    void shouldThrowWhenProviderIsNull() {
        assertThatThrownBy(() -> factory.getStrategy(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment provider cannot be null");
    }

    @Test
    @DisplayName("Should return all supported payment providers")
    void shouldReturnSupportedProviders() {
        List<PaymentProvider> providers = factory.getSupportedProviders();
        assertThat(providers).containsExactlyInAnyOrder(
                PaymentProvider.STRIPE,
                PaymentProvider.RAZORPAY,
                PaymentProvider.MOCK
        );
    }
}
