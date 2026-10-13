package com.payflow.commonclient.config;

import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Logs request headers, body, metadata for debugging
    }
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
    /**
     * Financial safety rule: Never retry requests automatically to prevent duplicate debits.
     */
    @Bean
    public Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }
}
