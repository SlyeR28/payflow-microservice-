package com.payflow.apigateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApiGatewayApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Verify Spring application context loads cleanly")
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    @DisplayName("Verify UserDetailsServiceAutoConfiguration is excluded and no default in-memory user manager exists")
    void testNoUserDetailsServiceBeanExists() {
        assertThrows(NoSuchBeanDefinitionException.class, () ->
                applicationContext.getBean(UserDetailsService.class),
                "No UserDetailsService should be configured in stateless JWT api-gateway; prevents default security password generation");
    }

    @Test
    @DisplayName("Verify SecurityFilterChain bean is present and configured")
    void testSecurityFilterChainConfigured() {
        SecurityFilterChain filterChain = applicationContext.getBean(SecurityFilterChain.class);
        assertNotNull(filterChain, "SecurityFilterChain must be active in api-gateway");
    }
}
