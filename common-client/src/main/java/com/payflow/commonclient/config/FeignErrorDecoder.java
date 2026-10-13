package com.payflow.commonclient.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.common.exceptions.BusinessException;
import com.payflow.common.exceptions.ForbiddenException;
import com.payflow.common.exceptions.ResourceNotFoundException;
import com.payflow.common.exceptions.UnauthorizedException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.InputStream;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        String message = "Downstream service error";

        try {
            if (response.body() != null) {
                try (InputStream bodyIs = response.body().asInputStream()) {
                    JsonNode root = objectMapper.readTree(bodyIs);
                    if (root.has("message") && !root.get("message").isNull()) {
                        message = root.get("message").asText();
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Could not extract error message from Feign response body for {}: {}", methodKey, ex.getMessage());
        }

        HttpStatus status = HttpStatus.resolve(response.status());
        if (status == null) {
            return defaultDecoder.decode(methodKey, response);
        }

        return switch (status) {
            case NOT_FOUND -> new ResourceNotFoundException(message);
            case BAD_REQUEST, CONFLICT -> new BusinessException(message, "DOWNSTREAM_ERROR");
            case UNAUTHORIZED -> new UnauthorizedException(message);
            case FORBIDDEN -> new ForbiddenException(message);
            default -> defaultDecoder.decode(methodKey, response);
        };
    }
}
