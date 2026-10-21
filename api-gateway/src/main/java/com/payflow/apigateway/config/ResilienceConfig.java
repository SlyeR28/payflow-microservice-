package com.payflow.apigateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer(){

        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)

                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .build());
        }


    }




/**
 * in central configuration of resilience4j there is no need of time limiter
 * the order we will follow is rate-limiter -> retry -> circuit breaker
 * 1 -> first simple step -> server will limit the requests per second
 * 2 -> if any of service goes down or show some intrepeted nature then server will go under retry
 * 3- > at last if all fails then circuit breaker will be opened
 */