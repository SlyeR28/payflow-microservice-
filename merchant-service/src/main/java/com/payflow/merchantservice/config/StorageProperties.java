package com.payflow.merchantservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.storage.s3")
public class StorageProperties {

    private String endpoint;
    private String bucketName;
    private String accessKey;
    private String secretKey;
    private String region = "us-east-1";

}
