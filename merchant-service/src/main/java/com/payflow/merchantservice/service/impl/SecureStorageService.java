package com.payflow.merchantservice.service.impl;

import com.payflow.merchantservice.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecureStorageService {

    private final StorageProperties storageProperties;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;


    @PostConstruct
    public void init(){
        try {
            ensureBucketExists();
        } catch (Exception e) {
            log.warn("MinIO bucket connectivity check on startup: {}", e.getMessage());
        }
    }

    public void ensureBucketExists(){
        String bucketName = storageProperties.getBucketName();

        try{
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        }catch (NoSuchBucketException e){
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            log.info("Created S3/mino bucket: {}", bucketName);
        }
    }

    public String generateUploadPresignedUrl(String objectKey ,
                                             String contentType ,
                                             Duration duration){
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(storageProperties.getBucketName())
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(duration)
                .putObjectRequest(putRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest)
                .url().toString();
    }

    public String generateDownloadPresignedUrl(String objectKey, Duration duration) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(storageProperties.getBucketName())
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

}
