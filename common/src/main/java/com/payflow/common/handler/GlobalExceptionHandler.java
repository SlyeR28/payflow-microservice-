package com.payflow.common.handler;

import com.payflow.common.dto.ErrorResponse;
import com.payflow.common.dto.ValidationErrorResponse;
import com.payflow.common.exceptions.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBase(BaseException ex , HttpServletRequest request){
        log.warn("Business Exception: {} - {}", ex.getErrorCode() ,ex.getMessage());

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(ex.getStatus().value())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    // ----- Validation Exception------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex , HttpServletRequest request
    ){
        Map<String , String> fieldsErrors = new HashMap<>();
        for(FieldError error : ex.getBindingResult().getFieldErrors()){
            fieldsErrors.put(error.getField() , error.getDefaultMessage());
        }

        log.warn("Validation failed for {} - {}" , request.getRequestURI() , fieldsErrors);

        ValidationErrorResponse body = ValidationErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("VALIDATION_ERROR")
                .message("Invalid Request")
                .path(request.getRequestURI())
                .fieldsErrors(fieldsErrors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }


    // ----- Security Exception -----

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse>handleAuthentication(
            AuthenticationException ex , HttpServletRequest request){

        log.warn("Authentication failed for {} - {}" , request.getRequestURI() , ex.getMessage());

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .errorCode("UNAUTHORIZED")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse>handleAccessDenied(
            AccessDeniedException ex , HttpServletRequest request){
        log.warn("Access denied for {}: {}", request.getRequestURI() , ex.getMessage());

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.FORBIDDEN.value())
                .errorCode("FORBIDDEN")
                .message("Access Denied")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }


    // resource not found exception remaining

     @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(
             NoResourceFoundException ex, HttpServletRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .errorCode("RESOURCE_NOT_FOUND")
                .message("Resource not found: " + request.getRequestURI())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception at {}", request.getRequestURI(), ex);

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }


 }
