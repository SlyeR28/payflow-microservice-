package com.payflow.common.util;

import com.payflow.common.dto.ApiResponse;
import com.payflow.common.dto.ErrorResponse;
import com.payflow.common.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public class SmokeTest {
    public static void main(String[] args) {
        // 1. Build an ApiResponse
        ApiResponse<String> ok = ApiResponse.sucess("Hello");
        System.out.println("ApiResponse.success = " + ok.isSuccess() + ", data=" + ok.getData());

        // 2. Build an ErrorResponse
        ErrorResponse err = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(404)
                .errorCode("RESOURCE_NOT_FOUND")
                .message("User not found")
                .path("/api/v1/users/99")
                .build();
        System.out.println("ErrorResponse = " + err.getErrorCode() + " -> " + err.getMessage());

        // 3. Throw  a ResourceNotFoundException
        try {
            throw new ResourceNotFoundException("User", 99L);
        } catch (ResourceNotFoundException ex) {
            System.out.println("Exception status = " + ex.getStatus());
            System.out.println("Exception code   = " + ex.getErrorCode());
            System.out.println("Exception msg    = " + ex.getMessage());
        }

        // 4. Verify BaseException hierarchy
        System.out.println("Is RuntimeException? " +
                (new ResourceNotFoundException("x") instanceof RuntimeException));
        System.out.println("Status is NOT_FOUND? " +
                (new ResourceNotFoundException("x").getStatus() == HttpStatus.NOT_FOUND));
    }
}