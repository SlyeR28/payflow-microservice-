package com.payflow.notificationservice.service;

import com.payflow.notificationservice.service.impl.TemplateEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateEngineServiceTest {

    private TemplateEngineService templateEngineService;

    @BeforeEach
    void setUp() {
        templateEngineService = new TemplateEngineService();
    }

    @Test
    @DisplayName("render: Should return empty string when template is null")
    void render_NullTemplate_ReturnsEmpty() {
        assertThat(templateEngineService.render(null, Map.of("key", "value"))).isEmpty();
    }

    @Test
    @DisplayName("render: Should return original template when variables are empty or null")
    void render_EmptyVariables_ReturnsOriginalTemplate() {
        String template = "Hello [[${name}]], your OTP is {{otp}}";
        assertThat(templateEngineService.render(template, null)).isEqualTo(template);
        assertThat(templateEngineService.render(template, Map.of())).isEqualTo(template);
    }

    @Test
    @DisplayName("render: Should replace both [[${key}]] and {{key}} tokens correctly")
    void render_ValidVariables_ReplacesTokens() {
        String template = "Hello [[${customerName}]], payment of {{amount}} for order [[${orderId}]] was successful.";
        Map<String, Object> vars = Map.of(
                "customerName", "Alice",
                "amount", "₹2,500.00",
                "orderId", "ORD-998877"
        );

        String result = templateEngineService.render(template, vars);

        assertThat(result).isEqualTo("Hello Alice, payment of ₹2,500.00 for order ORD-998877 was successful.");
    }
}
