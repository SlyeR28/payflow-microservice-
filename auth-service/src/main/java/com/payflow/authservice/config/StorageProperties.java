package com.payflow.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.storage.s3")
public class StorageProperties {

    private String endpoint = "http://192.168.1.18/:9000";
    private String bucketName = "user-avatars";
    private String accessKey = "admin_payflow_master";
    private String secretKey = "StrongMasterSecretKey#2026Secure";
    private String region = "us-east-1";

}
